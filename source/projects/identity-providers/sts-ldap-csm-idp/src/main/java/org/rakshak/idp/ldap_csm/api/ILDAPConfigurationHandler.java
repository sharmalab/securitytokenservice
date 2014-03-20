package org.rakshak.idp.ldap_csm.api;

import java.util.Set;

import org.rakshak.core.api.model.User;
import org.rakshak.idp.ldap_csm.ext.LDAPConfiguration;

public interface ILDAPConfigurationHandler {

	public  User getUser(String username, LDAPConfiguration ldapConfiguration) throws Exception;
	public  Set<User> getListOfUsers(LDAPConfiguration ldapConfiguration) throws Exception;
	
}
