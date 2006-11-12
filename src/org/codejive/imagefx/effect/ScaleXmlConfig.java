package org.codejive.imagefx.effect;

import org.codejive.common.config.ConfigurationException;
import org.codejive.imagefx.DomElementConfiguration;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ScaleXmlConfig implements DomElementConfiguration {
	private int targetWidth, targetHeight, targetSize;

	public void init(Element _configuration) {
		NodeList elems = _configuration.getElementsByTagName("scale");
		if (elems.getLength() != 1) {
			throw new ConfigurationException("Missing scale element");
		}
		Element scaleElem = (Element)elems.item(0);
		targetWidth = parseNum(scaleElem.getAttribute("width"));
		targetHeight = parseNum(scaleElem.getAttribute("height"));
		targetSize = parseNum(scaleElem.getAttribute("size"));
		if ((targetSize == -1) && (targetWidth == -1) && (targetHeight == -1)) {
			throw new ConfigurationException("Missing size, width or height attributes");
		}
		if ((targetSize > 0) && ((targetWidth > 0) || (targetHeight > 0))) {
			throw new ConfigurationException("Can't combine size and width/height attributes");
		}
	}

	private int parseNum(String _number) {
		int result;
		if ((_number != null) && (_number.length() > 0)) {
			try {
				result = Integer.parseInt(_number);
			} catch (NumberFormatException e) {
				throw new ConfigurationException(e);
			}
		} else {
			result = -1;
		}
		return result;
	}

	public int getTargetHeight() {
		return targetHeight;
	}

	public void setTargetHeight(int targetHeight) {
		this.targetHeight = targetHeight;
	}

	public int getTargetSize() {
		return targetSize;
	}

	public void setTargetSize(int targetSize) {
		this.targetSize = targetSize;
	}

	public int getTargetWidth() {
		return targetWidth;
	}

	public void setTargetWidth(int targetWidth) {
		this.targetWidth = targetWidth;
	}

}
