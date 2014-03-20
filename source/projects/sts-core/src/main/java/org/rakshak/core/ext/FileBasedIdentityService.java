package org.rakshak.core.ext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rakshak.core.api.AbstractSimpleIdentityService;
import org.rakshak.core.api.exception.AuthenticationException;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.exception.MethodNotImplementedException;
import org.rakshak.core.api.model.Credential;
import org.rakshak.core.api.model.Group;
import org.rakshak.core.api.model.IdentityServiceRegistration;
import org.rakshak.core.api.model.SecureToken;
import org.rakshak.core.api.model.ServiceOperation;
import org.rakshak.core.api.model.User;
import org.rakshak.opensaml2.core.TokenGenerator;
import org.rakshak.opensaml2.core.TokenParams;

public class FileBasedIdentityService extends AbstractSimpleIdentityService{

	
	private FileBasedIdentityServiceConfiguration configuration;
	private Map<String,User> users;
	private Map<String,Group> groups;
	private Map<String,String> usernamePasswordStore;
	
	
	
	private Log log = LogFactory.getLog(getClass());
	
	
	public void setup(IdentityServiceRegistration serviceRegistration) throws IdentityProviderException
	{
		configuration = FileBasedIdentityServiceConfiguration.convert(serviceRegistration.getConfiguration());
		try {
			configuration.validate();
			users = new HashMap<String, User>();
			groups = new HashMap<String, Group>();
			usernamePasswordStore = configuration.getUserStore();
			
			for(String user : usernamePasswordStore.keySet())
			{
				User usr = new  User();
				usr.setName(user);
				usr.setGroups(new HashSet<String>());
				this.users.put(user, usr);
			}
			
			for(String grp : configuration.getGroupStore().keySet())
			{
				Group group = new Group();
				group.setName(grp);
				group.setUsers(new HashSet<String>());
				
				for(String usr : configuration.getGroupStore().get(grp))
				{
					User user = this.users.get(usr);
					if(user!=null)
					{
						group.getUsers().add(usr);
						user.getGroups().add(grp);
					}
				}
				
				this.groups.put(grp, group);				
			}
			
		} catch (Exception e) {
			log.error(e);
			throw new IdentityProviderException(FileBasedIdentityProvider.class.getName() , e);
		}
		
		
		super.setup(serviceRegistration);
	}
	
	
	public boolean authenticate(Credential credential) {
		if(credential.getUsername()!=null && credential.getPassword()!=null )
		{
			if(usernamePasswordStore!=null && usernamePasswordStore.get(credential.getUsername())!=null && usernamePasswordStore.get(credential.getUsername()) . equals(credential.getPassword()))
			return true;
		}
		return false;
	}
	
	
	public SecureToken issueToken(Credential credential , String serviceProvider)
			throws IdentityProviderException, AuthenticationException {
		if(authenticate(credential))
		{
			Map<String,List<String>> parameters = new HashMap<String, List<String>>();
			List<String> values = new ArrayList<String>();
			User usr  = users.get(credential.getUsername());
			if(usr.getGroups()!=null && usr.getGroups().size() > 0)
			{
				for(String grp : usr.getGroups())
				{
					values.add(grp);
				}
			}
			
			parameters.put("groups", values);
			IdentityServiceRegistration serviceReg = getServiceRegistration();
			TokenParams tokenParams = new TokenParams();
			try {
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
		return users.values();
	}

	public Collection<Group> getGroups() throws IdentityProviderException {
		return groups.values();
	}

	
	public Group removeGroup(String group) throws IdentityProviderException {
		throw new MethodNotImplementedException(FileBasedIdentityProvider.class.getName(), ServiceOperation.removeGroup);
	}


	public User lookupUserByName(String username)
			throws IdentityProviderException {
		
		return this.users.get(username);
	}

}
