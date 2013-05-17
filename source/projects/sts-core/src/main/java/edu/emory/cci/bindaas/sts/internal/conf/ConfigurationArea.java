package edu.emory.cci.bindaas.sts.internal.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jamierf.persistenthashmap.util.FileUtils;

public class ConfigurationArea {

	private String id;
	private File current;
	
	public ConfigurationArea(File parent, String id)
	{
		this.id = id;
		this.current = new File(parent, id);
	}
	
	/**
	 * config area exists or not
	 * @return
	 */
	
	public boolean exists()
	{ 
		return this.current.exists() && this.current.isDirectory();
	}
	
	/**
	 * Setup config area
	 */
	
	public void setup()
	{
		this.current.mkdir();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Clean config area. Remove all files
	 */
	public void clean()
	{
		FileUtils.deleteDirectory(current);
	}
	
	/**
	 * Store content in filename in this config area
	 * @param filename
	 * @param content
	 * @throws IOException 
	 */
	
	public void store(String filename , byte[] content ) throws IOException
	{
		File file = new File(current, filename);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(content);
		fos.close();	
	}
	
	/**
	 * Retrieve file by filename from the config area
	 * @param filename
	 * @throws IOException 
	 */
	
	public InputStream retieve(String filename) throws IOException
	{
		File file = new File(current, filename);
		return new FileInputStream(file);
	}
	
}
