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
 * Created on July 15, 2006
 */
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
