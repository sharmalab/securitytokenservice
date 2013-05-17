package edu.emory.cci.bindaas.sts.ext;

import edu.emory.cci.bindaas.sts.api.IIdentityProvider;
import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;

public class FileBasedIdentityProvider implements IIdentityProvider {

	public IIdentityService createService(IdentityServiceRegistration serviceReg)
			throws IdentityProviderException {
		
		return null;
	}

}
