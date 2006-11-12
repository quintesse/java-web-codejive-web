/*
 * Created on Sep 27, 2005
 */
package org.codejive.web.weblog;

import java.net.URL;
import java.util.Collection;

import javax.xml.stream.XMLStreamReader;

public interface Blog {
	public static final String NAMESPACE = "http://www.codejive.org/NS/portico/weblog";
	
	public String getTitle();
	public XMLStreamReader getDescription();
	public URL getLink();

	public Collection<BlogItem> getItems();
}


/*
 * $Log:	$
 */