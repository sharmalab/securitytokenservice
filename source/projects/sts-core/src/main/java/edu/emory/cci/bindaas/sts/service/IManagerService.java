package edu.emory.cci.bindaas.sts.service;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceNotFoundException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;

public interface IManagerService {
	public IdentityServiceRegistration registerService(String identityProviderId, String name , String description, JsonObject configuration) throws IdentityProviderException;
	public IdentityServiceRegistration removeService(String id) throws IdentityProviderException;
	public IIdentityService getService(String id) throws IdentityProviderException, IdentityServiceNotFoundException;
}
