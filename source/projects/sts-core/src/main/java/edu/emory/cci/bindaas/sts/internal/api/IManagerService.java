package edu.emory.cci.bindaas.sts.internal.api;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;

public interface IManagerService {
	public IdentityServiceRegistration registerService(String identityProviderId, String description, JsonObject configuration) throws IdentityProviderException;
	public IdentityServiceRegistration removeService(String id) throws IdentityProviderException;
	public IdentityServiceRegistration getService(String id) throws IdentityProviderException;
}
