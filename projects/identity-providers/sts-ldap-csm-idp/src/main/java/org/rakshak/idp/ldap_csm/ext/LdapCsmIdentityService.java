package org.rakshak.idp.ldap_csm.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rakshak.core.api.AbstractSimpleIdentityService;
import org.rakshak.core.api.exception.AuthenticationException;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.model.Credential;
import org.rakshak.core.api.model.Group;
import org.rakshak.core.api.model.IdentityServiceRegistration;
import org.rakshak.core.api.model.SecureToken;
import org.rakshak.core.api.model.User;
import org.rakshak.core.util.JDBCDriverRegistry;
import org.rakshak.idp.ldap_csm.api.ICSMConfigurationHandler;
import org.rakshak.idp.ldap_csm.api.ILDAPConfigurationHandler;
import org.rakshak.opensaml2.core.TokenGenerator;
import org.rakshak.opensaml2.core.TokenParams;

public class LdapCsmIdentityService extends AbstractSimpleIdentityService{

	
	private LdapCsmIdentityServiceConfiguration configuration;
	private JDBCDriverRegistry jdbcDriverRegistry;
	private ILDAPConfigurationHandler ldapConfigHandler;
	private ICSMConfigurationHandler csmConfigurationHandler;
	
	
	private Log log = LogFactory.getLog(getClass());
	
	
	public void setup(IdentityServiceRegistration serviceRegistration) throws IdentityProviderException
	{
		configuration = LdapCsmIdentityServiceConfiguration.convert(serviceRegistration.getConfiguration());
		try {
			configuration.validate(jdbcDriverRegistry);
			
		} catch (Exception e) {
			log.error(e);
			throw new IdentityProviderException(LdapCsmIdentityProvider.class.getName() , e);
		}
		
		
		super.setup(serviceRegistration);
	}
	
	
	public boolean authenticate(Credential credential) throws IdentityProviderException{
		if(credential.getUsername()!=null && credential.getPassword()!=null )
		{
			boolean result = LDAPConfiguration.authenticate( configuration .getLdapConfiguration() , credential.getUsername(), credential.getPassword());
			return result;
		}
		return false;
	}
	
	
	public SecureToken issueToken(Credential credential , String serviceProvider)
			throws IdentityProviderException, AuthenticationException {
		if(authenticate(credential))
		{
			Map<String,List<String>> parameters = new HashMap<String, List<String>>();
			IdentityServiceRegistration serviceReg = getServiceRegistration();
			
			try {
			Set<String> values = CSMConfiguration.getGroups(credential.getUsername(), configuration.getCsmConfiguration() , csmConfigurationHandler);
			parameters.put("groups", new ArrayList<String>(values));
			
			User user = LDAPConfiguration.getUser(credential.getUsername(), configuration.getLdapConfiguration(), ldapConfigHandler);
			
			if(user.getFirstName()!=null)
			{
				parameters.put("First Name", Arrays.asList(user.getFirstName()));
			}
			
			if(user.getLastName()!=null)
			{
				parameters.put("Last Name", Arrays.asList(user.getLastName()));
			}
			
			if(user.getEmail()!=null)
			{
				parameters.put("Email", Arrays.asList(user.getEmail()));
			}
			
				TokenParams tokenParams = new TokenParams();
				tokenParams.setCredential(super.getKeyPair());
				tokenParams.setIssuer(serviceReg.getName());
				tokenParams.setLifetime(configuration.getTokenLifetime());
				tokenParams.setServiceProvider(serviceProvider);
				tokenParams.setCustomProperties(parameters);
				tokenParams.setSubject(credential.getUsername());
				String tokenString = TokenGenerator.generateToken(tokenParams);
				SecureToken token = new SecureToken(tokenString);
				
				return token;
			} catch (Exception e) {
				log.error(e);
				throw new IdentityProviderException(serviceReg.getId(), e);
			}
			
			
		}
		else
			throw new AuthenticationException(getServiceRegistration().getId());
	}

	public boolean validateToken(SecureToken secureToken)
			throws IdentityProviderException {
		IdentityServiceRegistration serviceReg = getServiceRegistration();
		try {
			
			boolean validationResult = TokenGenerator.validateSignature(secureToken.getContent(), getKeyPair());
			return validationResult;
			
		}catch(Exception e)
		{
			log.error(e);
			throw new IdentityProviderException(serviceReg.getId() , e);
		}
	}

	public Collection<User> getUsers() throws IdentityProviderException {
		Set<User> listOfUsers;
		try {
			listOfUsers = LDAPConfiguration.getListOfUsers(configuration.getLdapConfiguration() , ldapConfigHandler);
			for(User user : listOfUsers)
			{
				Set<String> groups = CSMConfiguration.getGroups(user.getName(), configuration.getCsmConfiguration() ,csmConfigurationHandler);
				user.setGroups(groups);
			}
			return listOfUsers;
		} catch (Exception e) {
			throw new IdentityProviderException(LdapCsmIdentityProvider.class.getName(), e);
		}
		
	}

	public Collection<Group> getGroups() throws IdentityProviderException {
		Set<Group> groups;
		try {
			groups = CSMConfiguration.getGroups(configuration.getCsmConfiguration() , csmConfigurationHandler);
			return groups;
		} catch (Exception e) {
				throw new IdentityProviderException(LdapCsmIdentityProvider.class.getName() , e);
		}
	
	}

	public JDBCDriverRegistry getJdbcDriverRegistry() {
		return jdbcDriverRegistry;
	}


	public void setJdbcDriverRegistry(JDBCDriverRegistry jdbcDriverRegistry) {
		this.jdbcDriverRegistry = jdbcDriverRegistry;
	}


	public ILDAPConfigurationHandler getLdapConfigHandler() {
		return ldapConfigHandler;
	}


	public void setLdapConfigHandler(ILDAPConfigurationHandler ldapConfigHandler) {
		this.ldapConfigHandler = ldapConfigHandler;
	}


	public ICSMConfigurationHandler getCsmConfigurationHandler() {
		return csmConfigurationHandler;
	}


	public void setCsmConfigurationHandler(
			ICSMConfigurationHandler csmConfigurationHandler) {
		this.csmConfigurationHandler = csmConfigurationHandler;
	}


	public User lookupUserByName(String username)
			throws IdentityProviderException {
		
		try {
			return LDAPConfiguration.getUser(username, configuration.getLdapConfiguration() , ldapConfigHandler);
		} catch (Exception e) {
			throw new IdentityProviderException(LdapCsmIdentityProvider.class.getName()	, e);
		}
	}

}
