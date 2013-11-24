package edu.emory.cci.bindaas.sts.service;

import java.util.List;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.sts.api.IIdentityProvider;
import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceAlreadyExistException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceNotFoundException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;

public interface IManagerService {
	public IdentityServiceRegistration registerService(String identityProviderId, String name , String description, JsonObject configuration , boolean overwrite) throws IdentityProviderException , IdentityServiceAlreadyExistException;
	public IdentityServiceRegistration registerService(IIdentityProvider identityProvider, String name , String description, JsonObject configuration , boolean overwrite) throws IdentityProviderException , IdentityServiceAlreadyExistException;
	public IdentityServiceRegistration removeService(String id) throws IdentityProviderException;
	public IIdentityService getService(String id) throws IdentityProviderException, IdentityServiceNotFoundException;
	public List<IdentityServiceRegistration> getAllService();
	public List<IIdentityProvider> getIdentityProviders();
}
