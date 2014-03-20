package org.rakshak.idp.ldap.ext;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.DefaultAttribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.model.Group;
import org.rakshak.core.api.model.User;

import com.google.gson.annotations.Expose;



public class LDAPConfiguration {
	
	@Expose private String ldapHost;
	@Expose private Integer ldapPort; 
	@Expose private String dnTemplate; /** Template for constructing Distinguished Name (DN) : cn=%s,ou=Emory **/
	@Expose private String bindUsername;
	@Expose private String bindPassword;
	@Expose private String searchUsersLdapQuery;
	@Expose private String searchGroupsLdapQuery;
	@Expose private String baseDN;
	
	public LDAPConfiguration()
	{
		searchGroupsLdapQuery = "ou=groups";
		searchUsersLdapQuery = "ou=people" ;
		
	}
	
	
	
	public String getLdapHost() {
		return ldapHost;
	}

	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}

	public Integer getLdapPort() {
		return ldapPort;
	}

	public void setLdapPort(Integer ldapPort) {
		this.ldapPort = ldapPort;
	}

	public String getBaseDN() {
		return baseDN;
	}

	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}



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
			 String testUsername = ldapConfiguration.getBindUsername();
			 String testPassword = ldapConfiguration.getBindPassword();
			 
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
	
	public String getDnTemplate() {
		return dnTemplate;
	}
	public void setDnTemplate(String dnTemplate) {
		this.dnTemplate = dnTemplate;
	}
	
	
	
	public String getBindUsername() {
		return bindUsername;
	}

	public void setBindUsername(String bindUsername) {
		this.bindUsername = bindUsername;
	}

	public String getBindPassword() {
		return bindPassword;
	}

	public void setBindPassword(String bindPassword) {
		this.bindPassword = bindPassword;
	}

	public String getSearchGroupsLdapQuery() {
		return searchGroupsLdapQuery;
	}

	public void setSearchGroupsLdapQuery(String searchGroupsLdapQuery) {
		this.searchGroupsLdapQuery = searchGroupsLdapQuery;
	}

	public synchronized static boolean authenticate(LDAPConfiguration ldapConfiguration , String username , String password) throws IdentityProviderException
	{
		String dn = ldapConfiguration.constructDN(username);
		
		
		LdapConnection connection = new LdapNetworkConnection( ldapConfiguration.ldapHost , ldapConfiguration.ldapPort );
		
		try {
			connection.bind( dn , password );
			log.debug("LDAP Auth succeeded for [" + dn + "]");
			return true;
		} catch (LdapException e) {
			log.error("Failed LDAP Auth using DN [" + dn  +"]",e);
			return false;
		}
		finally{
			try {
				connection.close();
			} catch (IOException e) {
				log.error("Could not close LDAP Connection");
			}
		}

		
	}
	
	public  synchronized  Set<User> getListOfUsers() throws Exception
	{
		LdapConnection connection = new LdapNetworkConnection( ldapHost , ldapPort );
		Set<User> listOfUsers = new HashSet<User>();
		Map<String,Set<String>> userGroupMap = new HashMap<String, Set<String>>();
		
		try {
		String distinguishedUsername = constructDN(this.bindUsername);	
		connection.bind( distinguishedUsername , this.bindPassword);
		EntryCursor cursorUsers = connection.search( searchUsersLdapQuery + "," + baseDN,"(cn=*)"   , SearchScope.ONELEVEL );
		EntryCursor cursorGroups = connection.search( searchGroupsLdapQuery + "," + baseDN, "(cn=*)"   , SearchScope.ONELEVEL );
		
		while ( cursorGroups.next() )
		{
		    Entry entry = cursorGroups.get();   
		    String groupName = entry.get("cn").getString();
		    Iterator<Value<?>> iter = entry.get("uniqueMember").iterator();
		    
		    while(iter.hasNext())
		    {
		    	Value<?> val = iter.next();
		    	String member = val.getValue().toString();
		    	Set<String> set = userGroupMap.get(member);
		    	if(set == null )
		    	{
		    		set = new HashSet<String>();
		    		userGroupMap.put(member, set);
		    	}
		    	
		    	set.add(groupName);
		    }
		}

		while ( cursorUsers.next() )
		{
		    Entry entry = cursorUsers.get();
		    String cn = entry.get("cn").getString();
		    String givenName = entry.get("givenName") !=null ? entry.get("givenName").getString() : cn;
		    String sn = entry.get("sn") !=null ? entry.get("sn").getString() : cn;
		    String email = entry.get("mail") !=null ? entry.get("mail").getString() : cn + "@example.org";
		    
		    String dn = entry.getDn().toString();
		    
		    User user = new User();
		    user.setGroups(userGroupMap.get(dn));
		    user.setEmail(email);
		    user.setFirstName(givenName);
		    user.setLastName(sn);
		    user.setName(cn);
		    listOfUsers.add(user);
		}
		
		}finally{
			connection.close();
		}
		
		
		return listOfUsers;
	}
	
	public synchronized  User getUser(String username ) throws Exception
	{
		LdapConnection connection = new LdapNetworkConnection( ldapHost , ldapPort );
		
		Map<String,Set<String>> userGroupMap = new HashMap<String, Set<String>>();
		
		try {
		String distinguishedUsername = constructDN(this.bindUsername);	
		connection.bind( distinguishedUsername , this.bindPassword);
		EntryCursor cursorUsers = connection.search( searchUsersLdapQuery + "," + baseDN, "(cn=" + username + ")" , SearchScope.ONELEVEL );
		EntryCursor cursorGroups = connection.search( searchGroupsLdapQuery + "," + baseDN, "(cn=*)"   , SearchScope.ONELEVEL );
		
		while ( cursorGroups.next() )
		{
		    Entry entry = cursorGroups.get();   
		    String groupName = entry.get("cn").getString();
		    Iterator<Value<?>> iter = entry.get("uniqueMember").iterator();
		    
		    while(iter.hasNext())
		    {
		    	Value<?> val = iter.next();
		    	String member = val.getValue().toString();
		    	Set<String> set = userGroupMap.get(member);
		    	if(set == null )
		    	{
		    		set = new HashSet<String>();
		    		userGroupMap.put(member, set);
		    	}
		    	
		    	set.add(groupName);
		    }
		}

		
			if(cursorUsers.next() && cursorUsers.available())
			{
				Entry entry = cursorUsers.get();   
			    String givenName = entry.get("givenName").getString();
			    String sn = entry.get("sn").getString();
			    String email = entry.get("mail").getString();
			    String cn = entry.get("cn").getString();
			    
			    String dn = entry.getDn().toString();
			    
			    User user = new User();
			    user.setGroups(userGroupMap.get(dn));
			    user.setEmail(email);
			    user.setFirstName(givenName);
			    user.setLastName(sn);
			    user.setName(cn);
			    return user;
			}
			else
				return null;
		
		
		}finally{
			connection.close();
		}
		
		
		

	}
	
	public synchronized  Set<Group> getListOfGroups() throws Exception
	{
		LdapConnection connection = new LdapNetworkConnection( ldapHost , ldapPort );
		
		Set<Group> listOfGroups = new HashSet<Group>();
		
		try {
		String distinguishedUsername = constructDN(this.bindUsername);	
		connection.bind( distinguishedUsername , this.bindPassword);
		
		EntryCursor cursorGroups = connection.search( searchGroupsLdapQuery + "," + baseDN, "(cn=*)"   , SearchScope.ONELEVEL );
		
		while ( cursorGroups.next() )
		{
		    Entry entry = cursorGroups.get();   
		    String groupName = entry.get("cn").getString();
		    Iterator<Value<?>> iter = entry.get("uniqueMember").iterator();
		    Group group = new Group();
		    group.setName(groupName);
		    group.setUsers(new HashSet<String>());
		    
		    if(entry.contains(new DefaultAttribute("description")))
		    {
		    	group.setDescription(entry.get("description").getString());
		    }
		    
		    while(iter.hasNext())
		    {
		    	Value<?> val = iter.next();
		    	String member = val.getValue().toString();
		    	EntryCursor cursorUsers = connection.search( member , "(cn=*)" , SearchScope.OBJECT );
		    	if(cursorUsers.next() && cursorUsers.available()){
		    		member = cursorUsers.get().get("cn").getString();
		    		
		    		if(isUserExist(member, connection))
		    		{
		    			group.getUsers().add(member);
		    		}
		    	}
		    	
		    	
		    }
		    
		    listOfGroups.add(group);
		}

		}finally{
			connection.close();
		}		
		
		return listOfGroups;
	}
	
	public  synchronized Group getGroup(String groupName) throws Exception {
		LdapConnection connection = new LdapNetworkConnection(ldapHost,
				ldapPort);

		try {
			String distinguishedUsername = constructDN(this.bindUsername);
			connection.bind(distinguishedUsername, this.bindPassword);

			EntryCursor cursorGroups = connection.search(searchGroupsLdapQuery
					+ "," + baseDN, "(cn=" + groupName + ")",
					SearchScope.ONELEVEL);

			if(cursorGroups.next() && cursorGroups.available())
			{
				Entry entry = cursorGroups.get();
				Iterator<Value<?>> iter = entry.get("uniqueMember").iterator();
				Group group = new Group();
				group.setName(groupName);
				group.setUsers(new HashSet<String>());
				
				if(entry.get("description")!=null)
				{
					group.setDescription(entry.get("description").getString());
				}
				
				while (iter.hasNext()) {
					Value<?> val = iter.next();
					String member = val.getValue().toString();
					EntryCursor cursorUsers = connection.search( member , "(cn=*)" , SearchScope.OBJECT ); 
			    	if(cursorUsers.next() && cursorUsers.available()){
			    		member = cursorUsers.get().get("cn").getString();
			    		if(isUserExist(member, connection))
			    		{
			    			group.getUsers().add(member);
			    		}
			    		
			    	}
					
				}

				return group;

			}
			else
				return null;
			
		} finally {
			connection.close();
		}

	}

	
	private  synchronized boolean isGroupExist(String groupName , LdapConnection connection) throws Exception
	{
		EntryCursor cursorGroups = connection.search(searchGroupsLdapQuery
				+ "," + baseDN, "(cn=" + groupName + ")",
				SearchScope.ONELEVEL);

		if(cursorGroups.next() && cursorGroups.available())
		{
			return true;
		}
		else
			return false;
	}
	
	private  synchronized boolean isUserExist(String userName , LdapConnection connection) throws Exception
	{
		EntryCursor cursorUser = connection.search(searchUsersLdapQuery
				+ "," + baseDN, "(cn=" + userName + ")",
				SearchScope.ONELEVEL);

		if(cursorUser.next() && cursorUser.available())
		{
			return true;
		}
		else
			return false;
	}

	
	
	public  synchronized User assignUserToGroups(String username, Collection<String> groups)
			throws Exception {
		
		LdapConnection connection = new LdapNetworkConnection(ldapHost,
				ldapPort);

		try {
			String distinguishedUsername = constructDN(this.bindUsername);
			connection.bind(distinguishedUsername, this.bindPassword);
			
			
			
			
			for(String groupName : groups)
			{
				if(!isGroupExist(groupName , connection))
					throw new Exception("Group does not exist [" + groupName + "]");
			}
			
			
			for(String groupName : groups)
			{
				// modify group and add this user
				Group group = getGroup(groupName);
				group.getUsers().add(username);
				updateGroup(group);
			}
			
			User user = getUser(username);
			return user;
						
		} finally {
			connection.close();
		}
	}


	public synchronized Group createOrModifyGroup(String groupName, String description,
			Collection<String> users, boolean createGroupIfNotExist)
			throws Exception {
		LdapConnection connection = new LdapNetworkConnection(ldapHost,
				ldapPort);

		try {
			String distinguishedUsername = constructDN(this.bindUsername);
			connection.bind(distinguishedUsername, this.bindPassword);
			
			Group group = getGroup(groupName);
			
			if(	group == null)
			{
				
				if(createGroupIfNotExist)
				{
					String groupDN = String.format("cn=%s,%s,%s", groupName , searchGroupsLdapQuery , baseDN);
					DefaultEntry groupEntry = new DefaultEntry(groupDN);
					groupEntry.add(new DefaultAttribute("cn", groupName));
					groupEntry.add(new DefaultAttribute("objectClass", "top"));
					groupEntry.add(new DefaultAttribute("objectClass", "groupOfUniqueNames"));
					groupEntry.add(new DefaultAttribute("description", description));
					
					for(String usr : users )
					{
						if(isUserExist(usr, connection))
						{
							String usrDN = String.format("cn=%s,%s,%s" , usr , searchUsersLdapQuery , baseDN);
							groupEntry.add(new DefaultAttribute("uniqueMember", usrDN));
						}
						else
						{
							throw new Exception("Trying to add user [" + usr + "] to group [" + groupName +  "] failed. User does not exist in Database");
						}
					}
					
					// add default member
					groupEntry.add(new DefaultAttribute("uniqueMember", String.format("%s,%s" , searchGroupsLdapQuery , baseDN)));
					
					connection.add(groupEntry);
					
					group = new Group();
					group.setName(groupName);
					group.setDescription(description);
					group.setUsers(new HashSet<String>(users));
				}
				else
				{
					throw new Exception("Group [" + groupName + "] does not exist");
				}
				
			}
			else
			{
				group.getUsers().addAll(users);
				updateGroup(group);
			}
			
			return group;
									
		} finally {
			connection.close();
		}
	}


	public synchronized Group removeGroup(String groupName) throws Exception {
		LdapConnection connection = new LdapNetworkConnection(ldapHost,
				ldapPort);

		try {
			String distinguishedUsername = constructDN(this.bindUsername);
			connection.bind(distinguishedUsername, this.bindPassword);
			
			Group group = getGroup(groupName);
			if(group!=null)
			{
				String groupDN = String.format("cn=%s,%s,%s", group.getName() , searchGroupsLdapQuery , baseDN);
				connection.delete(groupDN);
			}
			else
			{
				log.warn("Nothing to delete. Group [" + groupName + "] does not exist");
			}
			
			return group;
						
		} finally {
			connection.close();
		}
	}


	public synchronized Group removeUsersFromGroup(String groupName, Collection<String> users)
			throws Exception {
		
		LdapConnection connection = new LdapNetworkConnection(ldapHost,
				ldapPort);
		
		try {
			String distinguishedUsername = constructDN(this.bindUsername);
			connection.bind(distinguishedUsername, this.bindPassword);
			
			Group group = getGroup(groupName);
			if(group!=null)
			{
				group.getUsers().removeAll(users);
				updateGroup(group);
				return group;
			}
			else
			{
				throw new Exception("Cannot complete request removeUsersFromGroup. Group [" + groupName + "] does not exist");
			}
			
			
						
		} finally {
			connection.close();
		}
	}


	public synchronized  void removeUsersFromAllGroups(Collection<String> users)
			throws Exception {
		LdapConnection connection = new LdapNetworkConnection(ldapHost,
				ldapPort);
		
		try {
			String distinguishedUsername = constructDN(this.bindUsername);
			connection.bind(distinguishedUsername, this.bindPassword);
			
			Set<Group> listOfAllGroups = getListOfGroups();
			
			for(Group group : listOfAllGroups)
			{
				group.getUsers().removeAll(users);
				updateGroup(group);
			}
						
		} finally {
			connection.close();
		}
	}

	
	
	
	// update existing group
	public  synchronized Group updateGroup(Group group) throws Exception 
	{
		
		LdapConnection connection = new LdapNetworkConnection(ldapHost,
				ldapPort);

		try {
				String distinguishedUsername = constructDN(this.bindUsername);
				connection.bind(distinguishedUsername, this.bindPassword);
				
				EntryCursor cursorGroups = connection.search(searchGroupsLdapQuery
						+ "," + baseDN, "(cn=" + group.getName() + ")",
						SearchScope.ONELEVEL);

				if(cursorGroups.next() && cursorGroups.available())
				{
					Entry entry = cursorGroups.get();
					String groupDN = entry.getDn().getName();
					connection.delete(groupDN);
					DefaultEntry modifiedGroupEntry = new DefaultEntry(groupDN);
					modifiedGroupEntry.add(new DefaultAttribute("cn", group.getName()));
					modifiedGroupEntry.add(new DefaultAttribute("objectClass", "top"));
					modifiedGroupEntry.add(new DefaultAttribute("objectClass", "groupOfUniqueNames"));
					modifiedGroupEntry.add(new DefaultAttribute("description", group.getDescription()));
					
					if(group.getUsers()!=null)
					{
						for(String usr : group.getUsers())
						{
							if(isUserExist(usr, connection))
							{
								String usrDN = String.format("cn=%s,%s,%s" , usr , searchUsersLdapQuery , baseDN);
								modifiedGroupEntry.add(new DefaultAttribute("uniqueMember", usrDN));
							}
							else
							{
								throw new Exception("Trying to add user [" + usr + "] to group [" + group.getName() +  "] failed. User does not exist in Database");
							}
						}
					}
					
					// add default member
					modifiedGroupEntry.add(new DefaultAttribute("uniqueMember", String.format("%s,%s" , searchGroupsLdapQuery , baseDN)));
					
					connection.add(modifiedGroupEntry);
					return group;

				}

						
		} finally {
			connection.close();
		}
		
		return null;
	}



	public void validate() throws Exception {
		if(baseDN == null || dnTemplate == null || ldapHost == null || ldapPort == null || bindUsername == null || bindPassword == null )
			throw new Exception("Field [baseDN|dnTemplate|ldapHost|ldapPort|bindUsername|bindPassword] not set");
		
	}
	
}
