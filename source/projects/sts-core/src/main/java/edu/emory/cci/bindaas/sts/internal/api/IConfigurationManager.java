package edu.emory.cci.bindaas.sts.internal.api;

import java.io.IOException;
import java.io.InputStream;

public interface IConfigurationManager {

	public void store(String id , String filename , byte[] content ) throws IOException;
	public void store(String id , String filename , String content ) throws IOException;
	public InputStream retrieve(String id, String filename) throws IOException;
	public void clean(String id) throws IOException;
}
