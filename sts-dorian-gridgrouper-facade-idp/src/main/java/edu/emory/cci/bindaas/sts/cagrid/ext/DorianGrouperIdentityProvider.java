package edu.emory.cci.bindaas.sts.cagrid.ext;

import edu.emory.cci.bindaas.sts.api.IIdentityProvider;
import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;

public class DorianGrouperIdentityProvider implements IIdentityProvider {

	public IIdentityService createService(IdentityServiceRegistration serviceReg)
			throws IdentityProviderException {
		DorianGrouperIdentityService service = new DorianGrouperIdentityService();
		service.setup(serviceReg);
		return service;
	}

	public String getDescription() {
		
		return "Dorian GridGrouper IdP";
	}

}
