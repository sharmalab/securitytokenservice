package org.rakshak.core.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.rakshak.core.service.IConfigurationManagerService;

public class DynamicObject<T> {

	private T defaultObject;
	private T currentObject;
	private final static String suffix = ".config.json";
	private String name;
	private String filename;
	private Boolean usingDefault;
	private IConfigurationManagerService configurationManager;
	private String parentId;

	public Boolean getUsingDefault() {
		return usingDefault;
	}

	public void setUsingDefault(Boolean usingDefault) {
		this.usingDefault = usingDefault;
	}

	private static Log log = LogFactory.getLog(DynamicObject.class);

	public DynamicObject(String parentId, String name, T defaultObject,
			BundleContext context,
			IConfigurationManagerService configurationManager) throws Exception {
		this.name = name;
		this.filename = name + suffix;
		this.defaultObject = defaultObject;
		this.configurationManager = configurationManager;
		this.parentId = parentId;

		init();

		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("name", this.name);
		props.put("parentId", parentId);
		props.put("type", defaultObject.getClass().getName());
		context.registerService(DynamicObject.class.getName(), this, props);

	}

	@SuppressWarnings("unchecked")
	public void init() throws Exception {
		if (defaultObject != null) {
			usingDefault = true;

			// read from the file
			try {
				InputStream is = configurationManager.retrieve(parentId,
						filename);
				currentObject = (T) GSONUtil.getGSONInstance().fromJson(
						new InputStreamReader(is), defaultObject.getClass());
				usingDefault = false;
			} catch (Exception e) {
				log.warn("Reading default properties for [" + this.name + "]");
				currentObject = defaultObject ;
				saveObject();
			}
		} else
			throw new Exception("Default values not set");
	}

	public T getObject() {
		return currentObject;
	}

	public void saveObject() throws Exception {
		synchronized (currentObject) {

			try {
				String content = GSONUtil.getGSONInstance().toJson(
						currentObject);
				configurationManager.store(parentId, filename, content);
			} catch (Exception e) {
				log.error(e);
				throw e;
			}
		}
	}

}
