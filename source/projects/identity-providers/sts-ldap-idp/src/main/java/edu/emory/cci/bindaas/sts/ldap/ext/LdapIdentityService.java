package edu.emory.cci.bindaas.sts.ldap.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.sts.api.AbstractIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.AuthenticationException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.api.model.SecureToken;
import edu.emory.cci.bindaas.sts.api.model.ServiceOperation;
import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.opensaml2.core.TokenGenerator;
import edu.emory.cci.bindaas.sts.opensaml2.core.TokenParams;

public class LdapIdentityService extends AbstractIdentityService{

	private static Set<ServiceOperation> allowedOperations;
	
	static {
		allowedOperations = new HashSet<ServiceOperation>();
		allowedOperations.add(ServiceOperation.issueToken);
		allowedOperations.add(ServiceOperation.validateToken);
		allowedOperations.add(ServiceOperation.getUsers);
		allowedOperations.add(ServiceOperation.getGroups);
	}
	
	private LdapIdentityServiceConfiguration configuration;
	
	
	
	
	private Log log = LogFactory.getLog(getClass());
	
	
	public void setup(IdentityServiceRegistration serviceRegistration) throws IdentityProviderException
	{
		configuration = LdapIdentityServiceConfiguration.convert(serviceRegistration.getConfiguration());
		try {
			configuration.validate();
			
		} catch (Exception e) {
			log.error(e);
			throw new IdentityProviderException(LdapIdentityProvider.class.getName() , e);
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
			Set<Group> values = configuration.getLdapConfiguration().getListOfGroups();
			List<String> groups = new ArrayList<String>();
			
			for(Group g : values)
			{
				groups.add(g.getName());
			}
			
			parameters.put("groups", groups);
			
			User user = configuration.getLdapConfiguration().getUser(credential.getUsername());
			
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
			listOfUsers = configuration.getLdapConfiguration().getListOfUsers();
			return listOfUsers;
		} catch (Exception e) {
			throw new IdentityProviderException(LdapIdentityProvider.class.getName(), e);
		}
		
	}

	public Collection<Group> getGroups() throws IdentityProviderException {
		Set<Group> groups;
		try {
			groups = configuration.getLdapConfiguration().getListOfGroups();
			return groups;
		} catch (Exception e) {
				throw new IdentityProviderException(LdapIdentityProvider.class.getName() , e);
		}
	
	}

	public User addUser(String username, Collection<String> groups)
			throws IdentityProviderException {

		try {
			return null;
		} catch (Exception e) {
				throw new IdentityProviderException(LdapIdentityProvider.class.getName() , e);
		}
		
	}

	
	public boolean isOperationSupported(ServiceOperation serviceOperation) {
		return allowedOperations.contains(serviceOperation);
	}



	public User lookupUserByName(String username)
			throws IdentityProviderException {
		
		try {
			return configuration.getLdapConfiguration().getUser(username);
		} catch (Exception e) {
			throw new IdentityProviderException(LdapIdentityProvider.class.getName()	, e);
		}
	}


	public User assignUserToGroups(String username, Collection<String> groups)
			throws IdentityProviderException {
		try {
			return configuration.getLdapConfiguration().assignUserToGroups(username, groups);
		} catch (Exception e) {
			throw new IdentityProviderException(LdapIdentityProvider.class.getName()	, e);
		}
	}


	public Group createOrModifyGroup(String group, String description,
			Collection<String> users, boolean createGroupIfNotExist)
			throws IdentityProviderException {
		try {
			return configuration.getLdapConfiguration().createOrModifyGroup(group, description, users, createGroupIfNotExist);
		} catch (Exception e) {
			throw new IdentityProviderException(LdapIdentityProvider.class.getName()	, e);
		}
	}


	public Group removeGroup(String group) throws IdentityProviderException {
		try {
			return configuration.getLdapConfiguration().removeGroup(group);
		} catch (Exception e) {
			throw new IdentityProviderException(LdapIdentityProvider.class.getName()	, e);
		}
	}


	public Group removeUsersFromGroup(String group, Collection<String> users)
			throws IdentityProviderException {
		try {
			return configuration.getLdapConfiguration().removeUsersFromGroup(group, users);
		} catch (Exception e) {
			throw new IdentityProviderException(LdapIdentityProvider.class.getName()	, e);
		}
	}


	public void removeUsersFromAllGroups(Collection<String> users)
			throws IdentityProviderException {
		try {
			configuration.getLdapConfiguration().removeUsersFromAllGroups(users);
		} catch (Exception e) {
			throw new IdentityProviderException(LdapIdentityProvider.class.getName()	, e);
		}
		
	}


	
}
