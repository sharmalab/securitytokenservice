package edu.emory.cci.bindaas.sts.ldap_csm.ext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.api.model.User.Gender;
import edu.emory.cci.bindaas.sts.ldap_csm.api.ILDAPConfigurationHandler;

public class TCIALdapConfigurationHandler implements ILDAPConfigurationHandler {
	
	private static Log log = LogFactory.getLog(TCIALdapConfigurationHandler.class);
	
	public User getUser(String username, LDAPConfiguration ldapConfiguration)
			throws Exception {
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapConfiguration.getLdapUrl());
		env.put(Context.SECURITY_AUTHENTICATION, "none" );
		
		try {
	        LdapContext ctx = new InitialLdapContext(env, null);
	        ctx.setRequestControls(null);
	        SearchControls searchControls = new SearchControls();
	        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	        searchControls.setTimeLimit(30000);
	        
	        NamingEnumeration<?> namingEnum = ctx.search( ldapConfiguration.getSearchUsersLdapQuery() , "(cn=" + username + ")", searchControls);
	        User user = null;
	        if (namingEnum.hasMore ()) {
	            SearchResult result = (SearchResult) namingEnum.next ();    
	            Attributes attrs = result.getAttributes();
	            user =  getUserFromAttributes(username , attrs);

	        } 
	        namingEnum.close();
	        return user;
	    } catch (Exception e) {
	        log.error(e);
	        throw e;
	    }
		
	}

	public Set<User> getListOfUsers(LDAPConfiguration ldapConfiguration)
			throws Exception {
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapConfiguration.getLdapUrl());
		env.put(Context.SECURITY_AUTHENTICATION, "none" );
		Set<User> list = new HashSet<User>();

		try {
	        LdapContext ctx = new InitialLdapContext(env, null);
	        ctx.setRequestControls(null);
	        SearchControls searchControls = new SearchControls();
	        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	        searchControls.setTimeLimit(30000);
	        
	        NamingEnumeration<?> namingEnum = ctx.search( ldapConfiguration.getSearchUsersLdapQuery() , "(cn=*)", searchControls);
	        while (namingEnum.hasMore ()) {
	            SearchResult result = (SearchResult) namingEnum.next ();    
	            Attributes attrs = result.getAttributes();
	            User user = getUserFromAttributes(attrs);
	            list.add(user);
	        } 
	        namingEnum.close();
	    } catch (Exception e) {
	        log.error(e);
	        throw e;
	    }
		
		return list;

	}
	
	private User getUserFromAttributes(Attributes attrs) throws NamingException
	{
		
		User user = new User();
		user.setName(attrs.get("cn").get().toString());
		user.setEmail(getOptionalAttribute("mail" , attrs));
		user.setFirstName(getOptionalAttribute("givenname" , attrs));
		user.setLastName(getOptionalAttribute("sn" , attrs));
		user.setGender(Gender.UNKNOWN);
		
		Map<String,String> userAttrs = new HashMap<String,String>();
		NamingEnumeration<String> entries = attrs.getIDs();
		while(entries.hasMore())
		{
			String id = entries.next();
			String value = attrs.get(id).get().toString();
			
			userAttrs.put(id, value);
		}
		
		user.setAttributes(userAttrs);
		return user;
	}
	
	private String getOptionalAttribute(String name , Attributes attrs) throws NamingException
	{
		try{
			return attrs.get(name).get().toString();
			
		}catch(NullPointerException e)
		{
			return null;
		}
	}
	
	private User getUserFromAttributes(String username , Attributes attrs) throws NamingException
	{
		User user = getUserFromAttributes(attrs);
		user.setName(username);
		return user;
	}

}
