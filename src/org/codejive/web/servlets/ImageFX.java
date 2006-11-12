package org.codejive.web.servlets;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	private static AppConfig config = AppConfig.getInstance();
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Get next path element (after servlet context)
		String fullPath = request.getPathInfo();
		int p = fullPath.indexOf("/", 1);
		if (p >= 0) {
			String chainName = fullPath.substring(1, p);
			String imagePath = fullPath.substring(p + 1);
			
			File imageFile = new File(getServletContext().getRealPath(imagePath));
			if (!imageFile.exists()) {
				config.getLogger().info("ImageProcessor::doGet - '" + imageFile.getAbsolutePath() + "' does not exist");
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
				config.getLogger().info("ImageProcessor::doGet - Performing '" + chainName + "' on '" + imagePath + "'");
	
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