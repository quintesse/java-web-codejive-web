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
