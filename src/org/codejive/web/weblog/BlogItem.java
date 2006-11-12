/*
 * Created on Sep 27, 2005
 */
package org.codejive.web.weblog;

import java.net.URL;
import java.util.Date;

import javax.xml.stream.XMLStreamReader;

import org.codejive.common.CodejiveException;

public interface BlogItem extends Dated {
	public Date getDate() throws CodejiveException;
	public String getTitle() throws CodejiveException;
	public XMLStreamReader getSummary() throws CodejiveException;
	public XMLStreamReader getContent() throws CodejiveException;
	public MediaInfo[] getMedia();
	public URL getLink();
}


/*
 * $Log:	$
 */