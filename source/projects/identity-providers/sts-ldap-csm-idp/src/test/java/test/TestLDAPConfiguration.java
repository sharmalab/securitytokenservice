package test;

import org.rakshak.idp.ldap_csm.ext.LDAPConfiguration;
import org.rakshak.idp.ldap_csm.ext.TCIALdapConfigurationHandler;

public class TestLDAPConfiguration {

	public static void main(String[] args) throws Exception {
		LDAPConfiguration configuration = new LDAPConfiguration();
		configuration.setLdapUrl("ldap://10.28.163.89:1389");
		configuration.setDnTemplate("cn=%s,ou=Users,dc=cancerimagingarchive,dc=net");
		configuration.setSearchUsersLdapQuery("ou=Users,dc=cancerimagingarchive,dc=net");
		configuration.setTestPassword("");
		configuration.setTestUsername("");
		
//		Object obj = LDAPConfiguration.getListOfUsers(configuration , new TCIALdapConfigurationHandler());
//		System.out.println(obj);
		
		Object obj = LDAPConfiguration.getUser("nadirsagha", configuration, new TCIALdapConfigurationHandler());
		System.out.println(obj);
	}
}
