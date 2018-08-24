package org.rakshak.idp.cagrid.ext;

import org.rakshak.core.api.IIdentityProvider;
import org.rakshak.core.api.IIdentityService;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.model.IdentityServiceRegistration;

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
