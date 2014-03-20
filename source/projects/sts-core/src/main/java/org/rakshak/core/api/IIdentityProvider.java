package org.rakshak.core.api;

import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.model.IdentityServiceRegistration;

public interface IIdentityProvider {	
	
	public IIdentityService createService(IdentityServiceRegistration serviceReg) throws IdentityProviderException;
	public String getDescription();
}
