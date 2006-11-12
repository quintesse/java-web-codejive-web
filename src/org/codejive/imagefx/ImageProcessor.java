package org.codejive.imagefx;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.codejive.common.cache.FileUpdateMonitor;
import org.codejive.common.config.AppConfig;
import org.codejive.common.config.ConfigFiles;
import org.codejive.common.config.ConfigurationException;
import org.codejive.common.xml.SimpleNamespaceContext;
import org.codejive.common.xml.XPathHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Servlet implementation class for Servlet: ImageProcessor
 *
 */
public class ImageProcessor implements ImageEffect {
	private String chainName;
	private List<ImageEffect> effects;
	
	private static AppConfig config = AppConfig.getInstance();
	
	private static final String CONFIG_FILE_NAME = "imagefx.xml";
	
	public static final String NAMESPACE = "http://www.codejive.org/NS/portico/imagefx";
	public static final SimpleNamespaceContext CONTEXT = new SimpleNamespaceContext("fx", NAMESPACE);
	
	public static ImageProcessor getProcessor(String _chainName) throws ImageProcessorException {
		ImageProcessor proc = (ImageProcessor)config.getCache(ImageProcessor.class).get(_chainName);
		if (proc == null) {
			proc = new ImageProcessor(_chainName);

			File file = config.getPrivateResourceFile(CONFIG_FILE_NAME);
			FileUpdateMonitor mon = new FileUpdateMonitor(file);
			config.getCache(ImageProcessor.class).put(_chainName, proc, mon);
		}
		return proc;
	}
	
	private ImageProcessor(String _chainName) throws ImageProcessorException {
		chainName = _chainName;
		init();
	}
	
	private void init() throws ImageProcessorException {
		effects = new LinkedList<ImageEffect>();
		// Read the configuration file
		Document configDoc = ConfigFiles.getPrivateDocument(CONFIG_FILE_NAME);

		// Find all the effect definitions and put them in a Map
		class EffectDef {
			public String effectClassName;
			public String configClassName;
			
			public EffectDef(String _effectClassName, String _configClassName) {
				effectClassName = _effectClassName;
				configClassName = _configClassName;
			}
		}
		HashMap<String, EffectDef> effectDefs = new HashMap<String, EffectDef>();
		NodeList effectsList = XPathHelper.selectNodes(configDoc, "/fx:imagefx/fx:effects/fx:effectdef", CONTEXT);
		for (int i = 0; i < effectsList.getLength(); i++) {
			Element effectDefElem = (Element)effectsList.item(i);
			String name = effectDefElem.getAttribute("name");
			EffectDef def = new EffectDef(effectDefElem.getAttribute("class"), effectDefElem.getAttribute("configclass"));
			effectDefs.put(name, def);
		}

		// Find all the filter chains
		Element chainElem = null;
		NodeList chainList = XPathHelper.selectNodes(configDoc, "/fx:imagefx/fx:chains/fx:chain", CONTEXT);
		for (int i = 0; i < chainList.getLength(); i++) {
			Element elem = (Element)chainList.item(i);
			// Check if we found the right chain
			if (elem.getAttribute("name").equals(chainName)) {
				chainElem = elem;
				break;
			}
		}
		if (chainElem == null) {
			throw new ImageProcessorException("Unknown effects chain '" + chainName + "'");
		}
		
		// Find al the filters within the chain
		NodeList effectList = XPathHelper.selectNodes(chainElem, "fx:effect", CONTEXT);
		for (int j = 0; j < effectList.getLength(); j++) {
			Element effectElem = (Element)effectList.item(j);
			// Create the filter
			String effectName = effectElem.getAttribute("ref");
			if (!effectDefs.containsKey(effectName)) {
				throw new ConfigurationException("Could not find effect '" + effectName + "'");
			}
			EffectDef def = effectDefs.get(effectName);
			ImageEffect effect;
			try {
				effect = (ImageEffect)Class.forName(def.effectClassName).newInstance();
			} catch (Exception e) {
				throw new ConfigurationException("Could not create effect '" + def.effectClassName + "'", e);
			}
			if (effect instanceof ConfigurableImageEffect<?>) {
				// Create its configuration
				DomElementConfiguration effectConfig;
				try {
					// Initialize it
					effectConfig = (DomElementConfiguration)Class.forName(def.configClassName).newInstance();
					effectConfig.init(effectElem);
				} catch (Exception e) {
					throw new ConfigurationException("Could not create effect configuration '" + def.configClassName + "'", e);
				}
				try {
					// And assign it to the effect
					((ConfigurableImageEffect)effect).setConfiguration(effectConfig);
				} catch (Exception e) {
					throw new ConfigurationException("Could not configure effect '" + def.effectClassName + "' using '" + def.configClassName + "'", e);
				}
			}
			// And add it to the chain
			effects.add(effect);
		}
	}
	
	public BufferedImage process(BufferedImage _image) {
		for (ImageEffect effect : effects) {
			_image = effect.process(_image);
		}
		return _image;
	}
}
