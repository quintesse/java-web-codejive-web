/*
 * [codejive-common] Codejive commons package
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
 * Created on July 15, 2006
 */
package org.codejive.imagefx.effect;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.codejive.imagefx.ConfigurableImageEffect;

public class ScaleEffect implements ConfigurableImageEffect<ScaleXmlConfig> {
	private ScaleXmlConfig configuration;
	
	public void setConfiguration(ScaleXmlConfig _configuration) {
		configuration = _configuration;
	}
	
	public BufferedImage process(BufferedImage _image) {
		int w, h;
		int tw = configuration.getTargetWidth();
		int th = configuration.getTargetHeight();
		if (configuration.getTargetSize() > 0) {
			// The size will be applied to the largest side of the image
			// and the image will be resized maintaining aspect ratio
			if (_image.getWidth() > _image.getHeight()) {
				// Landscape
				tw = configuration.getTargetSize();
			} else {
				// Portrait (or perfectly square)
				th = configuration.getTargetSize();
			}
		}
		if ((tw > 0) && (th > 0)) {
			// Width and height are both explicitely supplied
			w = tw;
			h = th;
		} else {
			if (tw > 0) {
				// Width is supplied, we calculate height
				w = tw;
				float f = (float)w / _image.getWidth();
				h = (int)(_image.getHeight() * f);
			} else {
				// Height is supplied, we calculate width
				h = th;
				float f = (float)h / _image.getHeight();
				w = (int)(_image.getWidth() * f);
			}
		}
		BufferedImage scaledImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D gfx = scaledImage.createGraphics();
		gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		gfx.drawImage(_image, 0, 0, w, h, null);
		return scaledImage;
	}
}
