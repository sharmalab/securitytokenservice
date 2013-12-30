package test;

import java.util.Arrays;

import edu.emory.cci.bindaas.sts.ldap.ext.LDAPConfiguration;

public class TestLDAPConfiguration {

//	public static void main(String[] args) throws Exception {
//		LDAPConfiguration configuration = new LDAPConfiguration();
//		configuration.setLdapUrl("ldap://10.28.163.89:1389");
//		configuration.setDnTemplate("cn=%s,ou=Users,dc=cancerimagingarchive,dc=net");
//		configuration.setSearchUsersLdapQuery("ou=Users,dc=cancerimagingarchive,dc=net");
//		configuration.setTestPassword("");
//		configuration.setTestUsername("");
//		
////		Object obj = LDAPConfiguration.getListOfUsers(configuration , new TCIALdapConfigurationHandler());
////		System.out.println(obj);
//		
//		Object obj = LDAPConfiguration.getUser("nadirsagha", configuration, new TCIALdapConfigurationHandler());
//		System.out.println(obj);
//	}
	
	
	public static void main(String[] args) throws Exception {
		LDAPConfiguration configuration = new LDAPConfiguration();
		configuration.setLdapHost("localhost");
		configuration.setLdapPort(10389);
		configuration.setBaseDN("dc=sharmalab,dc=bmi,dc=emory,dc=edu");
		configuration.setDnTemplate("cn=%s,ou=people,dc=sharmalab,dc=bmi,dc=emory,dc=edu");
		configuration.setBindUsername("nadirsaghar");
		configuration.setBindPassword("temp");
		
//		System.out.println(configuration.getListOfUsers());
//		
//		System.out.println(configuration.getListOfGroups());
//		System.out.println(configuration.getGroup("Administrator"));
//		
//		System.out.println(configuration.getUser("nadirsaghar"));
		
		
//		System.out.println(configuration.addUser("Temp", "LastName", "temp@temp.temp", "temp1", Arrays.asList(new String[]{"Administrator"})));
		// get all groups
		
				System.out.println("===== Get All Groups ========");
				System.out.println(configuration.getListOfGroups());
				
				// get all users
				System.out.println("===== Get All Users ========");
				System.out.println(configuration.getListOfUsers());
				
				// add a new group public
				
				System.out.println("===== Create new group Public and add nadirsaghar ========");
				System.out.println(configuration.createOrModifyGroup("public", "public group", Arrays.asList(new String[]{"nadirsaghar"}), true));
				
				// add user nadirsaghar and ameen to public
				
				System.out.println("===== Adding ameen to Public ========");
				System.out.println(configuration.createOrModifyGroup("public", null , Arrays.asList(new String[]{"ameen"}), false));
				
				// display public
				
				System.out.println("===== Display Public ========");
				System.out.println(configuration.getGroup("public"));
				
				// display ameen
				
				System.out.println("===== Display Ameen ========");
				System.out.println(configuration.getUser("ameen"));
				
				// remove ameen from public
				
				System.out.println("===== Remove Ameen from Public ========");
				System.out.println(configuration.removeUsersFromGroup("public", Arrays.asList(new String[]{"ameen"})));
				
				// display public
				
				System.out.println("===== Display Public ========");
				System.out.println(configuration.getGroup("public"));
				
				// display ameen
				
				System.out.println("===== Display Ameen ========");
				System.out.println(configuration.getUser("ameen"));
				
				// add ameen to public
				
				System.out.println("===== Adding ameen to Public ========");
				System.out.println(configuration.createOrModifyGroup("public", null , Arrays.asList(new String[]{"ameen"}), false));
				
				// remove ameen from all groups
				
				System.out.println("===== Remove ameen from all Groups ========");
				configuration.removeUsersFromAllGroups( Arrays.asList(new String[]{"ameen"}));
				
				// get all groups
				
				System.out.println("===== Get All Groups ========");
				System.out.println(configuration.getListOfGroups());
				
				// remove public group
				
				System.out.println("===== Remove Public Groups ========");
				System.out.println(configuration.removeGroup("public"));
				
				// revert to original state
				
				System.out.println("===== Adding Ameen to Student ========");
				System.out.println(configuration.createOrModifyGroup("Student", null , Arrays.asList(new String[]{"ameen"}), false));

		System.out.println(configuration.createOrModifyGroup("Developers", null , Arrays.asList(new String[]{"ameen"}), false));
		
	}
}
