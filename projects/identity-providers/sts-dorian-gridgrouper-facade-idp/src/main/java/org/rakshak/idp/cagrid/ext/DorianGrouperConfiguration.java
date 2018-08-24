package org.rakshak.idp.cagrid.ext;

import org.rakshak.core.util.GSONUtil;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class DorianGrouperConfiguration {

	@Expose private String baseSTSUrl;
	@Expose private String host;
	@Expose private String protocol;
	@Expose private Integer port;

	public String getBaseSTSUrl() {
		return baseSTSUrl;
	}

	public void setBaseSTSUrl(String baseSTSUrl) {
		this.baseSTSUrl = baseSTSUrl;
	}

	public static DorianGrouperConfiguration convert(JsonObject configuration) {
		DorianGrouperConfiguration conf = GSONUtil.getGSONInstance().fromJson(configuration, DorianGrouperConfiguration.class);
		return conf;
	}

	public void validate() throws Exception {
		
		if(baseSTSUrl == null || host == null || port == null || protocol == null)
			throw new Exception("Attribute [baseSTSUrl|host|port|protocol] not set");
		
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
	
	
	
}
