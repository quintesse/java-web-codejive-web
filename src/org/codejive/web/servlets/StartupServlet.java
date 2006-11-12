package org.codejive.web.servlets;

import java.io.File;

import javax.servlet.ServletException;

import org.codejive.common.config.AppConfig;
import org.codejive.common.config.AppConfigImpl;

/**
 * Servlet implementation class for Servlet: StartupServlet
 *
 */
 public class StartupServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	@Override
	public void init() throws ServletException {
		AppConfig appConfig = AppConfig.getInstance();
//		 UGLY HACK BELOW!!!!!!!!!
		((AppConfigImpl)appConfig).addResourcePath(new File(getServletContext().getRealPath("/")));
		((AppConfigImpl)appConfig).addResourcePath(new File(getServletContext().getRealPath("/WEB-INF")));
		((AppConfigImpl)appConfig).addPrivateResourcePath(new File(getServletContext().getRealPath("/WEB-INF")));
	}   
}