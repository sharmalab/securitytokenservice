package org.rakshak.idp.ldap.ext;

import org.rakshak.core.util.GSONUtil;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class LdapIdentityServiceConfiguration {

	public final static Integer DEFAULT_TOKEN_LIFE = 3600;

	@Expose private LDAPConfiguration ldapConfiguration;
	 
	@Expose private String issuerName; 
	@Expose private Integer tokenLifetime;
	
	
	public String getIssuerName() {
		return issuerName;
	}
	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}
	public LDAPConfiguration getLdapConfiguration() {
		return ldapConfiguration;
	}
	public void setLdapConfiguration(LDAPConfiguration ldapConfiguration) {
		this.ldapConfiguration = ldapConfiguration;
	}
	
	public Integer getTokenLifetime() {
		return tokenLifetime;
	}
	public void setTokenLifetime(Integer tokenLifetime) {
		this.tokenLifetime = tokenLifetime;
	}
	
	
	public void validate() throws Exception /** validate all fields **/
	{
		if(issuerName == null) throw new Exception ("Field [issuerName] is missing");
		
		if(ldapConfiguration == null) throw new Exception("LDAPConfiguration not set");
		else {
			ldapConfiguration.validate();
		}
		LDAPConfiguration.testLDAPConnection(ldapConfiguration);
	}
	
	public static LdapIdentityServiceConfiguration convert(JsonObject config)
	{
		return GSONUtil.getGSONInstance().fromJson(config,LdapIdentityServiceConfiguration.class);
	}
		
	
	
}
