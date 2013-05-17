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
	
	public Collection<User> getUsers() throws IdentityProviderException;
	public Collection<Group> getGroups() throws IdentityProviderException;
	public User addUser(String username,Collection<String> groups) throws IdentityProviderException;
	public Group addGroup(String group,Collection<String> users) throws IdentityProviderException;
	public Group removeGroup(String group) throws IdentityProviderException;
	public User removeUser(String user) throws IdentityProviderException;
	public Group updateGroup(Group group) throws IdentityProviderException;
	public User updateUser(User user) throws IdentityProviderException;
	
	
	public IdentityServiceRegistration getRegistrationInfo() ;
	public boolean isOperationSupported(ServiceOperation serviceOperation);
}
