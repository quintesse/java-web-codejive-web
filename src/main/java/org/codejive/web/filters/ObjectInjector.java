/**
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
 * Created on November 15, 2006
 */
package org.codejive.web.filters;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.codejive.common.config.ConfigFiles;
import org.codejive.common.config.ConfigurationException;
import org.codejive.common.xml.DomValueReader;
import org.codejive.common.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author tako
 *
 */
public class ObjectInjector implements Filter {
	private FilterConfig filterConfig;
	
	private static final String CONFIG_FILE_EXTENSION = ".xoil";
	private static final String CONFIG_FILE_DEFAULT = "default" + CONFIG_FILE_EXTENSION;
	
	public static final String NAMESPACE = "http://www.codejive.org/NS/web/xoil";
	public static final SimpleNamespaceContext CONTEXT = new SimpleNamespaceContext("x", NAMESPACE);

	private static Logger logger = Logger.getLogger(ObjectInjector.class.getName());
	
	public ObjectInjector() {
		filterConfig = null;
	}
	
	public void init(FilterConfig _config) throws ServletException {
		filterConfig = _config;
	}

	public void doFilter(ServletRequest _request, ServletResponse _response, FilterChain _chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)_request;
		
		// Check if there is a configuration file associated with this URL
		String url = request.getServletPath();
		logger.info("Incoming URL: " + url);
		String configUrl = url + CONFIG_FILE_EXTENSION;
		String configPath = filterConfig.getServletContext().getRealPath(configUrl);
		File configFile = new File(configPath);
		handleXoil(configFile, request);
		_chain.doFilter(_request, _response);
	}

	public void destroy() {
		filterConfig = null;
	}
	
	private void handleXoil(File _configFile, HttpServletRequest _request) {
		if (_configFile.getName().equals(CONFIG_FILE_DEFAULT)) {
			// Before handling the default file in the current folder we handle the
			// default file in the parent folder (if we are not in the web root yet)
			String rootPath = stripEndSlash(filterConfig.getServletContext().getRealPath("/"));
			String configParentPath = stripEndSlash(_configFile.getParentFile().getParent());
			if (configParentPath.length() >= rootPath.length()) {
				File defaultConfig = new File(_configFile.getParentFile().getParentFile(), CONFIG_FILE_DEFAULT);
				handleXoil(defaultConfig, _request);
			}
		} else {
			// Before handling the requested file we handle any default
			// files in the same folder
			File defaultConfig = new File(_configFile.getParentFile(), CONFIG_FILE_DEFAULT);
			handleXoil(defaultConfig, _request);
		}
		handleXoilFile(_configFile, _request);
	}
	
	private void handleXoilFile(File _configFile, HttpServletRequest _request) {
		if (_configFile.exists()) {
			// Read and parse the configuration file
			Document configDoc = ConfigFiles.getPrivateDocument(_configFile.getAbsolutePath());
			DomValueReader reader = new DomValueReader(configDoc, CONTEXT);
			reader.goTo("x:page");
			if (reader.push("x:blocks") != null) {
				for (Node node : reader.elements("x:block")) {
					String name = reader.value("@name");
					String className = reader.value("@class");
					String context = reader.value("@context");
					logger.info("Block: " + name + ", " + className + ", " + context);
					// Check if the block already exists in the context
					if (getAttribute(_request, name) == null) {
						Object block;
						// If a class name has been provided
						if (className == null) {
							throw new ConfigurationException("Missing @class attribute");
						}
						try {
							// Load the indicated class
							Class c = Class.forName(className);
							// Check if it's a builder
							if (isImplementationOf(c, InstanceBuilder.class)) {
								// Create builder
								InstanceBuilder builder = (InstanceBuilder)c.newInstance();
								builder.init(null);
								// Let builder create a new object initializing it with
								// the content of the current Element
								block = builder.newInstance((Element)node);
							} else {
								// Create block
								block = c.newInstance();
								// If supported, initialize it with the content of the
								// current Element
								if (block instanceof ConfigurableInstance) {
									((ConfigurableInstance)block).init((Element)node);
								}
							}
						} catch (ClassNotFoundException e) {
							throw new ConfigurationException(e);
						} catch (SecurityException e) {
							throw new ConfigurationException(e);
						} catch (InstantiationException e) {
							throw new ConfigurationException(e);
						} catch (IllegalAccessException e) {
							throw new ConfigurationException(e);
						}
						// Add the block to the right context
						if ("application".equalsIgnoreCase(context)) {
							_request.getSession().getServletContext().setAttribute(name, block);
						} else if ("session".equalsIgnoreCase(context)) {
							_request.getSession().setAttribute(name, block);
						} else if ("page".equalsIgnoreCase(context) || (context == null)) {
							_request.setAttribute(name, block);
						}
						logger.info("Created: " + block);
					}
				}
			}
		}
	}
	
	private String stripEndSlash(String _path) {
		String res;
		if (_path.endsWith(File.separator)) {
			res = _path.substring(0, _path.length() - 1);
		} else {
			res = _path;
		}
		return res;
	}
	
	private Object getAttribute(HttpServletRequest _request, String _name) {
		Object result = _request.getAttribute(_name);
		if (result == null) {
			result = _request.getSession().getAttribute(_name);
			if (result == null) {
				result = _request.getSession().getServletContext().getAttribute(_name);
			}
		}
		return result;
	}
	
	private boolean isImplementationOf(Class _class, Class _interface) {
		Class[] interfaces = _class.getInterfaces();
		for (Class i : interfaces) {
			if (i == _interface) {
				return true;
			}
		}
		return false;
	}
}
