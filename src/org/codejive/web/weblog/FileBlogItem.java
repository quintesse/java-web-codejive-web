/*
 * Created on Sep 28, 2005
 */
package org.codejive.web.weblog;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.xml.stream.XMLStreamReader;

import org.codejive.common.CodejiveException;
import org.codejive.common.config.ConfigurationException;
import org.codejive.common.io.file.FileComparator;
import org.codejive.common.xml.DomValueReader;
import org.codejive.common.xml.SimpleNamespaceContext;
import org.codejive.common.xml.XmlHelper;
import org.codejive.common.xml.XmlInputFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FileBlogItem implements BlogItem {
	private FolderBlog blog;
	private File file;
	private Document doc;
	private SimpleNamespaceContext context;
	private String title;
	private Element summary;
	private Element content;
	private MediaInfo[] media;
	private boolean readMeadia;
	
	public FileBlogItem(FolderBlog _blog, File _file) throws CodejiveException {
		if (!_file.exists()) {
			throw new CodejiveException(_file.getAbsolutePath() + " does not exist");
		}
		if (!_file.isFile()) {
			throw new CodejiveException(_file.getAbsolutePath() + " is not a regular file");
		}
		blog = _blog;
		file = _file;
		doc = null;
		readMeadia = false;
		context = new SimpleNamespaceContext();
		context.setPrefix("blog", Blog.NAMESPACE);
	}

	private Document readFile() throws CodejiveException {
		if (doc == null) {
			doc = XmlHelper.readDocument(file);
			DomValueReader rdr = new DomValueReader(doc, context);
			rdr.goTo("blog:item");
			title = rdr.value("@title");
			content = rdr.element("blog:content");
			summary = rdr.element(".//blog:summary");
			if (summary == null) {
				summary = content;
			}
		}
		return doc;
	}
	
	public Date getDate() throws CodejiveException {
		readFile();
		int day = Integer.valueOf(file.getName().substring(0, 2));
		File dir = file.getParentFile();
		int month = Integer.valueOf(dir.getName()) - 1;
		dir = dir.getParentFile();
		int year = Integer.valueOf(dir.getName());
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, 12, 0, 0);
		return cal.getTime();
	}

	public String getTitle() throws CodejiveException {
		readFile();
		return title;
	}

	public XMLStreamReader getSummary() throws CodejiveException {
		readFile();
		XmlInputFactory fact = XmlInputFactory.newInstance();
		return fact.createDomReader(summary);
	}

	public XMLStreamReader getContent() throws CodejiveException {
		readFile();
		XmlInputFactory fact = XmlInputFactory.newInstance();
		return fact.createDomReader(content);
	}

	public MediaInfo[] getMedia() {
		if (!readMeadia) {
			File folder = file.getParentFile();
			String name = file.getName();
			File[] mediaFiles = folder.listFiles(new MediaFilter(name));
			if (mediaFiles.length > 0) {
				Arrays.sort(mediaFiles, new FileComparator(FileComparator.Order.ASCENDING));
				media = new MediaInfo[mediaFiles.length];
				for (int i = 0; i < mediaFiles.length; i++) {
					File mediaFile = mediaFiles[i];
					// TODO: See if there's extra info in the XML about this file
					media[i] = new MediaInfo(mediaFile, null, null);
				}
			} else {
				media = null;
			}
			readMeadia = true;
		}
		return media;
	}

	public URL getLink() {
		String rootPath = blog.getFolder().getAbsolutePath();
		String filePath = file.getAbsolutePath();
		String relPath = filePath.substring(rootPath.length() + 1);
		URL link;
		try {
			link = new URL(blog.getLink(), "index.jsp?article=" + relPath);
		} catch (MalformedURLException e) {
			throw new ConfigurationException(e);
		}
		return link;
	}

}

class MediaFilter implements FilenameFilter {
	File folder;
	String name;
	public MediaFilter(String _name) {
		name = _name.substring(0, _name.length() - 4) + "-";
	}
	public boolean accept(File _dir, String _name) {
		boolean ok = _name.startsWith(name) && (
					_name.endsWith(".jpg") || 
					_name.endsWith(".gif") || 
					_name.endsWith(".png"));
		return ok;
	}
}


/*
 * $Log:	$
 */