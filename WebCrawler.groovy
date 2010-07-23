package nazt
System.setProperty("file.encoding","TIS-620")
// the number of urls to process
MAX_URLS = 10000

// the number of concurrent crawler threads
NUM_THREADS = 10

 hit=0
// main data structures for urls
visitedURLs = new HashSet()
unvisitedURLs = new LinkedList()

// start url
unvisitedURLs << 'http://view.gprocurement.go.th/01_procure_egp/index.php'

// file to log urls
def file = new File('urls.log')
def parsed = new File('parsed.log')
def result=[]
/**
 *	Get (possibly wait for) next URL to process (thread save).
 */
synchronized nextURL() {
	if(visitedURLs.size() >= MAX_URLS) {
		return null
	}
	else { 
/*		println "IN ELSE"*/
		while(unvisitedURLs.isEmpty()) {
			try { 
				println "\t\twaiting..." ;
				if(hit>150) return null;
				wait(1000) 
				println "\t\t HIT = "+  ++hit 	
			}
			catch(InterruptedException e) {}
		}
		println "THIS"
		def url = unvisitedURLs.first()
		println "U Size = " + unvisitedURLs.size()
		unvisitedURLs.remove(url)
		visitedURLs << url
		return url
	}
}

/**
 *	Add new URL to list of unvisited URLs, if not already contained or processed earlier (thread save).
 */
synchronized addURL(newURL) {
	if(null != newURL && !visitedURLs.contains(newURL) && !unvisitedURLs.contains(newURL)) {		
		unvisitedURLs << newURL
		hit=0
		notifyAll()
	}
}


def fixURL(host, base, url) {
	def newURL
	def list=base.tokenize("/")

	if(url.startsWith("http://") || url.startsWith('https://') || url.startsWith('ftp://'))	// absolute url
		newURL = url
	else if(url.startsWith('/'))	// relative to host
		newURL = host + url
	else if(url.startsWith(".."))	// relative to host
	{
			newURL = new URL(base.toURL(),url.toString()).toString()
	}
	else if(url.startsWith('mailto:'))
		newURL = null
	else			
		newURL = base + url
	return newURL
}

///// start crawling /////

def startMillis = System.currentTimeMillis()

NUM_THREADS.times{
	sleep 100
	Thread.start {

		 
		def nekoParser = new org.cyberneko.html.parsers.SAXParser()
		nekoParser.setFeature('http://xml.org/sax/features/namespaces', false)			 
/*		nekoParser.setFeature('http://cyberneko.org/html/features/scanner/style/strip-comment-delims', true)	
		nekoParser.setFeature('http://cyberneko.org/html/features/scanner/script/strip-comment-delims', true)	*/
		nekoParser.setFeature('http://cyberneko.org/html/features/scanner/ignore-specified-charset', true)			
		nekoParser.setProperty('http://cyberneko.org/html/properties/default-encoding',"TIS-620") 		
		 
 			
		println "IN BODY"
		hit=0
		while(true) {
			try{
				def url = nextURL()
				if(null == url)
				{
					println("---> i Found NULL !")
					return
					
				}
				def host = (url =~ /(http:\/\/[^\/]+)\/?.*/)[0][1]
				def base = url[0..url.lastIndexOf('/')]		
				def page = new XmlSlurper(nekoParser).parse(url)
				cf_name = []
				url_list=url.replaceAll("http://","").tokenize("/")
				println "CF " + cf_name
				cf_name=url_list

				cf_name=cf_name.join("_")
				ofile=new File('contents/'+cf_name)
				ofile.append(page,"UTF-8")
				def links = page.depthFirst().grep{ it.name() == 'A' && it.@href.toString().contains("php") }.'@href'	 
				
				links.each { link ->
					def newURL = fixURL(host, base, link.toString())
					addURL(newURL)
					result<<newURL	
				}
				println ">>> ${Thread.currentThread().getName()} : Analyzing \"${url}\"... Found ${links.size()} links"
			}
			catch(Exception e) {
		 		println "Unexcepted Error: ${e.getMessage()}" 			 		
			}
		} 		
	}
}
def download(address, outfile)
{
    def file = new FileOutputStream(outfile)
    def out = new BufferedOutputStream(file)
try {
	out << new URL(address).openStream()
}
catch(Exception e) {
		println " Error: ${e.getMessage()}" 
}
    out.close()
}
// wait for all threads to die
while(Thread.activeCount() > 1) {
/*	int active = Thread.activeCount();
	    System.out.println("currently active threads: " + active);
	    def all  = new Thread[active];
	    Thread.enumerate(all);
	    for (int i = 0; i < active; i++) {
	      System.out.println(i + ": " + all[i]);
	    }*/
	sleep 100
	println "checking.."
}


println ("Sorting ... ")
 		result=result.unique().sort()
	 	println result.size()
/*	result.sort().unique().each { download(it,it.tokenize("/").last())}*/
	 result.each { file << it.toString().trim()+"\n" }
 
 
println System.getProperty("file.encoding");




	