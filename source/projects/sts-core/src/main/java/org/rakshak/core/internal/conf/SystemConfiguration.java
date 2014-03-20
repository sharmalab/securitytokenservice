package org.rakshak.core.internal.conf;

import com.google.gson.annotations.Expose;

public class SystemConfiguration {

	@Expose private String stsProtocol;
	@Expose private String stsHost;
	@Expose private Integer stsPort;
	@Expose private String adminIdentityService;
	
	
	
	public String getAdminIdentityService() {
		return adminIdentityService;
	}
	public void setAdminIdentityService(String adminIdentityService) {
		this.adminIdentityService = adminIdentityService;
	}
	public String  getStsBaseUrl()
	{
		return String.format("%s://%s:%s" , stsProtocol , stsHost , stsPort);
	}
	public String getStsHost() {
		return stsHost;
	}
	public void setStsHost(String stsHost) {
		this.stsHost = stsHost;
	}
	public Integer getStsPort() {
		return stsPort;
	}
	public void setStsPort(Integer stsPort) {
		this.stsPort = stsPort;
	}
	public String getStsProtocol() {
		return stsProtocol;
	}
	public void setStsProtocol(String stsProtocol) {
		this.stsProtocol = stsProtocol;
	}
	
	
}
