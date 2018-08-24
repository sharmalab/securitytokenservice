package org.rakshak.core.service;

import java.util.List;

import org.rakshak.core.api.IIdentityProvider;
import org.rakshak.core.api.IIdentityService;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.exception.IdentityServiceAlreadyExistException;
import org.rakshak.core.api.exception.IdentityServiceNotFoundException;
import org.rakshak.core.api.model.IdentityServiceRegistration;

import com.google.gson.JsonObject;

public interface IManagerService {
	public IdentityServiceRegistration registerService(String identityProviderId, String name , String description, JsonObject configuration , boolean overwrite) throws IdentityProviderException , IdentityServiceAlreadyExistException;
	public IdentityServiceRegistration registerService(IIdentityProvider identityProvider, String name , String description, JsonObject configuration , boolean overwrite) throws IdentityProviderException , IdentityServiceAlreadyExistException;
	public IdentityServiceRegistration removeService(String id) throws IdentityProviderException;
	public IIdentityService getService(String id) throws IdentityProviderException, IdentityServiceNotFoundException;
	public List<IdentityServiceRegistration> getAllService();
	public List<IIdentityProvider> getIdentityProviders();
}
