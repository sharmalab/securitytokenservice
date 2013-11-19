package edu.emory.cci.bindaas.sts.ldap_csm.api;

import java.util.Set;

import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.ldap_csm.ext.LDAPConfiguration;

public interface ILDAPConfigurationHandler {

	public  User getUser(String username, LDAPConfiguration ldapConfiguration) throws Exception;
	public  Set<User> getListOfUsers(LDAPConfiguration ldapConfiguration) throws Exception;
	
}
