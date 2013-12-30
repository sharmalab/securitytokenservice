package edu.emory.cci.bindaas.sts.ldap_csm.ext;

import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.ldap_csm.api.ILDAPConfigurationHandler;



public class LDAPConfiguration {
	
	@Expose private String ldapUrl; /** LDAP URI to authenticate users **/
	@Expose private String dnTemplate; /** Template for constructing Distinguished Name (DN) : cn=%s,ou=Emory **/
	@Expose private String testUsername;
	@Expose private String testPassword;
	@Expose private String searchUsersLdapQuery;
	
	private static Log log = LogFactory.getLog(LDAPConfiguration.class);
	
	public String constructDN(String username)
	{
		return String.format(dnTemplate, username);
	}
	
	/**
	 * checks if ldapConfiguration is valid
	 * @param ldapConfiguration
	 * @throws Exception
	 */
	public static void testLDAPConnection(LDAPConfiguration ldapConfiguration) throws Exception
	{
		try{
			 String testUsername = ldapConfiguration.getTestUsername();
			 String testPassword = ldapConfiguration.getTestPassword();
			 
			 if(testUsername!=null && testPassword!=null)
			 {
				 boolean result = authenticate(ldapConfiguration, testUsername , testPassword );
				 if(!result) throw new Exception("Cannot authenticate using test user/password");
			 }
			 
		}catch(Exception e)
		{
			throw e;
		}
	}
	
	public String getSearchUsersLdapQuery() {
		return searchUsersLdapQuery;
	}
	public void setSearchUsersLdapQuery(String searchUsersLdapQuery) {
		this.searchUsersLdapQuery = searchUsersLdapQuery;
	}
	public String getLdapUrl() {
		return ldapUrl;
	}
	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}
	public String getDnTemplate() {
		return dnTemplate;
	}
	public void setDnTemplate(String dnTemplate) {
		this.dnTemplate = dnTemplate;
	}
	public String getTestUsername() {
		return testUsername;
	}
	public void setTestUsername(String testUsername) {
		this.testUsername = testUsername;
	}
	public String getTestPassword() {
		return testPassword;
	}
	public void setTestPassword(String testPassword) {
		this.testPassword = testPassword;
	}
	
	public static boolean authenticate(LDAPConfiguration ldapConfiguration , String username , String password) throws IdentityProviderException
	{
		String dn = ldapConfiguration.constructDN(username);
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapConfiguration.getLdapUrl());
		env.put(Context.SECURITY_PRINCIPAL, dn );
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			new InitialDirContext(env);
			log.debug("LDAP Auth succeeded for [" + dn + "]");
			return true;
		} catch (NamingException e) {
			log.error("Failed LDAP Auth using DN [" + dn  +"]",e);
			return false;
		}

		
	}
	
	public static Set<User> getListOfUsers(LDAPConfiguration ldapConfiguration , ILDAPConfigurationHandler configHandler) throws Exception
	{
		return configHandler.getListOfUsers(ldapConfiguration);
	}
	
	public static User getUser(String username, LDAPConfiguration ldapConfiguration , ILDAPConfigurationHandler configHandler) throws Exception
	{
		return configHandler.getUser(username, ldapConfiguration);
	}
	
	
	
}
