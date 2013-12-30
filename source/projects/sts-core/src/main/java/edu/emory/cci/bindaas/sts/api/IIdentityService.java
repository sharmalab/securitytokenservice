package edu.emory.cci.bindaas.sts.api;

import java.util.Collection;

import edu.emory.cci.bindaas.sts.api.exception.AuthenticationException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.api.model.SecureToken;
import edu.emory.cci.bindaas.sts.api.model.ServiceOperation;
import edu.emory.cci.bindaas.sts.api.model.User;

public interface IIdentityService {

	public void setup(IdentityServiceRegistration serviceRegistration) throws IdentityProviderException;
	public void cleanup() throws IdentityProviderException;
	
	public SecureToken issueToken(Credential credential , String serviceProvider) throws IdentityProviderException,AuthenticationException;
	public boolean validateToken(SecureToken secureToken) throws IdentityProviderException;
	public boolean authenticate(Credential credential) throws IdentityProviderException,AuthenticationException;
	public User lookupUserByName(String username) throws IdentityProviderException;
	public Collection<User> getUsers() throws IdentityProviderException;
	public Collection<Group> getGroups() throws IdentityProviderException;
	
	
	// Add existing user to specified group(s)
	public User assignUserToGroups(String username,Collection<String> groups) throws IdentityProviderException;
	
	// Add specified users to this group. Create group if not already exist
	public Group createOrModifyGroup(String group, String description , Collection<String> users , boolean createGroupIfNotExist) throws IdentityProviderException;
	
	// Remove group
	public Group removeGroup(String group) throws IdentityProviderException;
	
	
	// Remove users from group
	public Group removeUsersFromGroup(String group , Collection<String> users) throws IdentityProviderException;
	
	public void removeUsersFromAllGroups(Collection<String> users) throws IdentityProviderException;
	
	public IdentityServiceRegistration getRegistrationInfo() ;
	public boolean isOperationSupported(ServiceOperation serviceOperation);
}
