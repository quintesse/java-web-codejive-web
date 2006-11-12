package org.codejive.web.filters;

import org.w3c.dom.Element;

public interface InstanceBuilder {
	void init(Element _config);
	Object newInstance(Element _config);
}
