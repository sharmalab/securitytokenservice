package edu.emory.cci.bindaas.sts.api;

import java.util.List;

import edu.emory.cci.bindaas.sts.api.exception.AuthenticationException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.api.model.SecureToken;
import edu.emory.cci.bindaas.sts.api.model.ServiceOperation;
import edu.emory.cci.bindaas.sts.api.model.User;

public interface IIdentityService {

	public SecureToken issueToken(Credential credential) throws IdentityProviderException,AuthenticationException;
	public boolean validateToken(SecureToken secureToken) throws IdentityProviderException,AuthenticationException;
	
	public List<User> getUsers() throws IdentityProviderException;
	public List<Group> getGroups() throws IdentityProviderException;
	public User addUser(String username,List<String> groups) throws IdentityProviderException;
	public Group addGroup(String group,List<String> users) throws IdentityProviderException;
	public Group removeGroup(String group) throws IdentityProviderException;
	public User removeUser(String user) throws IdentityProviderException;
	public Group updateGroup(Group group) throws IdentityProviderException;
	public User updateUser(User user) throws IdentityProviderException;
	
	
	public IdentityServiceRegistration getRegistrationInfo() throws IdentityProviderException;
	public boolean isOperationSupported(ServiceOperation serviceOperation);
}
