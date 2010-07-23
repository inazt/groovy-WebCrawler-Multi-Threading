def nekoParser = new org.cyberneko.html.parsers.SAXParser()
                nekoParser.setFeature('http://xml.org/sax/features/namespaces', false)
                nekoParser.setFeature('http://cyberneko.org/html/features/scanner/ignore-specified-charset', true)
                nekoParser.setProperty('http://cyberneko.org/html/properties/default-encoding',"TIS-620")
/*
def url="http://view.gprocurement.go.th/01_procure_egp/view_online_notice.php?id=387160&display_status=A"
		def page = new XmlSlurper(nekoParser).parse(url)
		
def links = page.depthFirst().grep{ println it.name();it.name() == 'A' }
def span = page.depthFirst().grep{ it.name()=="SPAN"  }
println links
println span

ofile=new File('./span.output')

span.eachWithIndex{ val , idx -> 		ofile.append( "${idx} ::-> ${val.DIV} \n" ,"UTF-8") }
*/
	
	url="http://view.gprocurement.go.th/01_procure_egp/index.php?page=1"
	page = new XmlSlurper(nekoParser).parse(url)
	links = page.depthFirst().grep{ it.name() == 'A' && it.@href.toString().contains("id")  } 
/*	ofile=new File('./span.output')
	span.eachWithIndex{ val , idx -> 		ofile.append( "${idx} ::-> ${val.DIV} \n" ,"UTF-8") }*/
	ofile=new File('./index')
	links.each { 
			m= it.@href =~ /^(.)*("id")*(.)*(\d{6})(.)*/ 
 	
			ofile.append( " --> ${m[0].get(4)} -> ${it.toString().trim()} \n" ,"UTF-8") 
			
	}
/*				def td=	 page.depthFirst().grep{ it.name() == 'TD' } */
/*				println ofile.append( " ${td.toString()}\n" ,"UTF-8") */
/*				td.eachWithIndex { i,j->ofile.append( "${i} ::: ${j.toString()}\n" ,"UTF-8")  }*/
		