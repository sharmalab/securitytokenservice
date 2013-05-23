package edu.emory.cci.bindaas.sts.internal.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import edu.emory.cci.bindaas.sts.internal.conf.ConfigurationArea;
import edu.emory.cci.bindaas.sts.service.IConfigurationManagerService;

public class ConfigurationManagerServiceImpl implements IConfigurationManagerService {
	private String configDirectory;
	public String getConfigDirectory() {
		return configDirectory;
	}

	public void setConfigDirectory(String configDirectory) {
		this.configDirectory = configDirectory;
	}

	private File parent;
	private Map<String,ConfigurationArea> cacheOfConfigrationArea;
	
	public void init() throws IOException
	{
		parent = new File(configDirectory);
		if(!parent.isDirectory())
		{
			boolean success = parent.mkdir();
			if(!success)
				throw new IOException("Cannot create configuration directory at [" + parent.getAbsolutePath() + "]");
		}
		
		cacheOfConfigrationArea = new HashMap<String, ConfigurationArea>();
	}

	public void store(String id, String filename, byte[] content)
			throws IOException {
		
		ConfigurationArea configArea = cacheOfConfigrationArea.get(id);
		if(configArea!=null)
		{
			configArea.store(filename, content);
		}
		else
		{
			configArea = new ConfigurationArea(parent, id);
			if(!configArea.exists())
			{
				configArea.setup();
			}
			configArea.store(filename, content);
			cacheOfConfigrationArea.put(id, configArea);
		}
		
	}

	public void store(String id, String filename, String content)
			throws IOException {
		ConfigurationArea configArea = cacheOfConfigrationArea.get(id);
		if(configArea!=null)
		{
			configArea.store(filename, content.getBytes());
		}
		else
		{
			configArea = new ConfigurationArea(parent, id);
			if(!configArea.exists())
			{
				configArea.setup();
			}
			configArea.store(filename, content.getBytes());
			cacheOfConfigrationArea.put(id, configArea);
		}
		
	}

	public InputStream retrieve(String id, String filename) throws IOException {
		ConfigurationArea configArea = cacheOfConfigrationArea.get(id);
		if(configArea!=null)
		{
			return configArea.retieve(filename);
		}
		else
		{
			configArea = new ConfigurationArea(parent, id);
			if(!configArea.exists())
			{
				configArea.setup();
			}
			cacheOfConfigrationArea.put(id, configArea);
			return configArea.retieve(filename);
		}
	}

	public void clean(String id) throws IOException {
		ConfigurationArea configArea = cacheOfConfigrationArea.get(id);
		if(configArea!=null)
		{
			configArea.clean();
			cacheOfConfigrationArea.remove(id);
		}
		else
		{
			configArea = new ConfigurationArea(parent, id);
			if(configArea.exists())
			{
				configArea.clean();
			}
		}		
	}
}
