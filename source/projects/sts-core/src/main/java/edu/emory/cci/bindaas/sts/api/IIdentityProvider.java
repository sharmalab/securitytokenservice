package edu.emory.cci.bindaas.sts.api;

import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;

public interface IIdentityProvider {	
	
	public IIdentityService createService(IdentityServiceRegistration serviceReg) throws IdentityProviderException;
	public String getDescription();
}
