/*
 * [codejive-web] Codejive web package
 * 
 * Copyright (C) 2006 Tako Schotanus
 * 
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 * 
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Created on Sep 27, 2005
 */
package org.codejive.web.weblog;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.xml.stream.XMLStreamReader;

import org.codejive.common.CodejiveException;
import org.codejive.common.io.file.ExtensionFilter;
import org.codejive.common.io.file.FileComparator;
import org.codejive.common.io.file.FolderFilter;
import org.codejive.common.xml.DomValueReader;
import org.codejive.common.xml.SimpleNamespaceContext;
import org.codejive.common.xml.XmlHelper;
import org.codejive.common.xml.XmlInputFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class finds all blog items from the folder specified in the constructor.
 * The folder structure is assumed to be as follows:
 * [root-folder]
 *    [4-digit-year]
 *       [2-digit-month]
 *          [2-digit-day].xml
 * The file naming convention for the XML files actually isn't that strict,
 * in fact they only have to end in .xml and when sorted alphabetically they
 * should end up in the order you want them (which is normally by date so that's
 * why giving them a 2-digit day number would work, but if you have more than one
 * message in a day you could just name them 01a.xml, 01b.xml, etc or 01-12h15.xml,
 * 01-12h30.xml or whatever you want).
 * 
 * @author tako
 */
public class FolderBlog implements Blog {
	private File folder;
	private URL rootUrl;

	private File infoFile;
	private Document infoDoc;
	private SimpleNamespaceContext context;

	private String title;
	private Element description;
	private ArrayList<BlogItem> items;
	
	public FolderBlog(File _folder, URL _rootUrl) throws CodejiveException {
		title = null;
		description = null;
		items = null;
		
		if (!_folder.exists()) {
			throw new CodejiveException(_folder.getAbsolutePath() + " does not exist");
		}
		if (!_folder.isDirectory()) {
			throw new CodejiveException(_folder.getAbsolutePath() + " is not a folder");
		}
		folder = _folder;
		rootUrl = _rootUrl;
		
		infoDoc = null;
		context = new SimpleNamespaceContext();
		context.setPrefix("blog", Blog.NAMESPACE);
		infoFile = new File(folder, "blog.xml");
		if (infoFile.exists()) {
			readInfoFile();
		}
	}

	private Document readInfoFile() throws CodejiveException {
		if (infoDoc == null) {
			infoDoc = XmlHelper.readDocument(infoFile);
			DomValueReader rdr = new DomValueReader(infoDoc, context);
			rdr.goTo("blog:blog");
			title = rdr.value("@title");
			description = rdr.element("blog:description");
		}
		return infoDoc;
	}
	
	public String getTitle() {
		return title;
	}

	public XMLStreamReader getDescription() {
		XmlInputFactory fact = XmlInputFactory.newInstance();
		return fact.createDomReader(description);
	}

	public URL getLink() {
		return rootUrl;
	}
	
	public File getFolder() {
		return folder;
	}

	/*
	 * This method finds all XML files from the folder specified in the constructor
	 * and adds them to one big list (so this obviously won't work for blogs with
	 * a lot of information!).
	 */
	private void fillItems() {
		// Double locking worked again in JVM 1.5, didn't it?
		// If not, what happens now will be REALLY STUPID!
		if (items == null) {
			synchronized (this) {
				if (items == null) {
					items = new ArrayList<BlogItem>();
					File[] years = folder.listFiles(new FolderFilter());
					Arrays.sort(years, new FileComparator(FileComparator.Order.DESCENDING));
					for (File yearFolder : years) {
						File[] months = yearFolder.listFiles(new FolderFilter());
						Arrays.sort(months, new FileComparator(FileComparator.Order.DESCENDING));
						for (File monthFolder : months) {
							File[] xmlFiles = monthFolder.listFiles(new ExtensionFilter("xml"));
							Arrays.sort(xmlFiles, new FileComparator(FileComparator.Order.DESCENDING));
							for (File file : xmlFiles) {
								try {
									BlogItem item = new FileBlogItem(this, file);
									items.add(item);
								} catch (CodejiveException e) {
									// We just ignore any errors and skip the file
								}
							}
						}
					}
				}
			}
		}
	}

	public Collection<BlogItem> getItems() {
		fillItems();
		return Collections.unmodifiableList(items);
	}
}

/*
 * $Log:	$
 */