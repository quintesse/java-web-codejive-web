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
 * Created on November 15, 2006
 */
package org.codejive.web.servlets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codejive.common.config.AppConfig;
import org.codejive.imagefx.ImageProcessor;
import org.codejive.imagefx.ImageProcessorException;

/**
 * Servlet implementation class for Servlet: ImageProcessor
 *
 */
public class ImageFX extends javax.servlet.http.HttpServlet {
	
	private static Logger logger = Logger.getLogger(ImageFX.class.getName());
	private static AppConfig config = AppConfig.getInstance();
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get next path element (after servlet context)
		String fullPath = request.getPathInfo();
		int p = fullPath.indexOf("/", 1);
		if (p >= 0) {
			String chainName = fullPath.substring(1, p);
			String imagePath = fullPath.substring(p + 1);
			
			File imageFile = new File(getServletContext().getRealPath(imagePath));
			if (!imageFile.exists()) {
				logger.info("ImageProcessor::doGet - '" + imageFile.getAbsolutePath() + "' does not exist");
				response.sendError(404);
				return;
			}

			File cacheDir = config.getPrivateResourceFile("cache");
			if (cacheDir == null) {
				throw new ServletException("Missing cache folder");
			}
			File outFile = new File(new File(new File(cacheDir, this.getClass().getName()), chainName), imagePath);

			// Check if a cached version of our processed image exists and if it's up-to-date
			boolean updated = (!outFile.exists() || (imageFile.lastModified() > outFile.lastModified()));
			if (updated) {
				logger.info("ImageProcessor::doGet - Performing '" + chainName + "' on '" + imagePath + "'");
	
				BufferedImage image = ImageIO.read(imageFile);
				if (image == null) {
					throw new ServletException("Could not read image");
				}
	
				try {
					ImageProcessor proc = ImageProcessor.getProcessor(chainName);
					image = proc.process(image);
				} catch (ImageProcessorException e) {
					throw new ServletException("Could not create image processor", e);
				}
				
				if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
					throw new ServletException("Could not create cache folder(s)");
				}
				if (!ImageIO.write(image, "JPG", outFile)) {
					throw new ServletException("Could not write image");
				}
			}
			
			response.setContentType("image/jpeg");
			OutputStream out = response.getOutputStream();
			InputStream is = new FileInputStream(outFile);
			byte buf[] = new byte[1024];
			int n = is.read(buf);
			while (n > 0) {
				out.write(buf, 0, n);
				n = is.read(buf);
			}
			out.flush();
		} else {
			throw new ServletException("Incomplete path");
		}
	}
}