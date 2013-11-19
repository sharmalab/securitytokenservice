package edu.emory.cci.bindaas.sts.ldap_csm.ext;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.sts.util.GSONUtil;
import edu.emory.cci.bindaas.sts.util.JDBCDriverRegistry;

public class LdapCsmIdentityServiceConfiguration {

	public final static Integer DEFAULT_TOKEN_LIFE = 3600;

	@Expose private LDAPConfiguration ldapConfiguration;
	@Expose private CSMConfiguration csmConfiguration; 
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
	public CSMConfiguration getCsmConfiguration() {
		return csmConfiguration;
	}
	public void setCsmConfiguration(CSMConfiguration csmConfiguration) {
		this.csmConfiguration = csmConfiguration;
	}
	public Integer getTokenLifetime() {
		return tokenLifetime;
	}
	public void setTokenLifetime(Integer tokenLifetime) {
		this.tokenLifetime = tokenLifetime;
	}
	
	
	public void validate(JDBCDriverRegistry jdbcDriverRegistry) throws Exception /** validate all fields **/
	{
		if(issuerName == null) throw new Exception ("Field [issuerName] is missing");
		CSMConfiguration.testCSMConnection(csmConfiguration , jdbcDriverRegistry);
		LDAPConfiguration.testLDAPConnection(ldapConfiguration);
	}
	
	public static LdapCsmIdentityServiceConfiguration convert(JsonObject config)
	{
		return GSONUtil.getGSONInstance().fromJson(config,LdapCsmIdentityServiceConfiguration.class);
	}
		
	
	
}
