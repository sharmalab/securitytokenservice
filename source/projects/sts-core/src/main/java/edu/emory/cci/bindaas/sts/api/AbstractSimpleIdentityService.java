package edu.emory.cci.bindaas.sts.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.exception.MethodNotImplementedException;
import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.api.model.ServiceOperation;
import edu.emory.cci.bindaas.sts.api.model.User;

public abstract class AbstractSimpleIdentityService extends AbstractIdentityService{

	private Set<ServiceOperation> unsupportedOperations;
	public AbstractSimpleIdentityService()
	{
		unsupportedOperations = new HashSet<ServiceOperation>();
		unsupportedOperations.addAll( Arrays.asList(new ServiceOperation[]{ ServiceOperation.assignUserToGroups , ServiceOperation.createOrModifyGroup , ServiceOperation.removeGroup 
				, ServiceOperation.removeUsersFromAllGroups , ServiceOperation.removeUsersFromGroup }));
	}
	
	public User assignUserToGroups(String username, Collection<String> groups)
			throws IdentityProviderException {
			throw new MethodNotImplementedException(getClass().getName(), ServiceOperation.assignUserToGroups);
	}

	public Group createOrModifyGroup(String group, String description,
			Collection<String> users, boolean createGroupIfNotExist)
			throws IdentityProviderException {
		throw new MethodNotImplementedException(getClass().getName(), ServiceOperation.createOrModifyGroup);
	}

	public Group removeGroup(String group) throws IdentityProviderException {
		throw new MethodNotImplementedException(getClass().getName(), ServiceOperation.removeGroup);
	}

	public Group removeUsersFromGroup(String group, Collection<String> users)
			throws IdentityProviderException {
		throw new MethodNotImplementedException(getClass().getName(), ServiceOperation.removeUsersFromGroup);
	}

	public void removeUsersFromAllGroups(Collection<String> users)
			throws IdentityProviderException {
		throw new MethodNotImplementedException(getClass().getName(), ServiceOperation.removeUsersFromAllGroups);
	}

	public boolean isOperationSupported(ServiceOperation serviceOperation) {
		if(unsupportedOperations.contains(serviceOperation))
			return false;
		else
			return true;
	}

}
