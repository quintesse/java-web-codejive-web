package org.codejive.imagefx;

public interface ConfigurableImageEffect<T extends Configuration> extends ImageEffect {
	void setConfiguration(T _configuration);
}
