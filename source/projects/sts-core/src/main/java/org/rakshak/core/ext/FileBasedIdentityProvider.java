package org.rakshak.core.ext;

import org.rakshak.core.api.IIdentityProvider;
import org.rakshak.core.api.IIdentityService;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.model.IdentityServiceRegistration;
import org.rakshak.core.service.IConfigurationManagerService;

public class FileBasedIdentityProvider implements IIdentityProvider {

	private IConfigurationManagerService configurationManager;
	private String keyPairFileName ;
	
	public String getKeyPairFileName() {
		return keyPairFileName;
	}

	public void setKeyPairFileName(String keyPairFileName) {
		this.keyPairFileName = keyPairFileName;
	}

	public IConfigurationManagerService getConfigurationManager() {
		return configurationManager;
	}

	public void setConfigurationManager(IConfigurationManagerService configurationManager) {
		this.configurationManager = configurationManager;
	}

	public IIdentityService createService(IdentityServiceRegistration serviceReg)
			throws IdentityProviderException {
		FileBasedIdentityService service = new FileBasedIdentityService();
		service.setConfigurationManager(this.configurationManager);
		service.setKeyPairFilename(this.keyPairFileName);
		service.setup(serviceReg);
		return service;
	}

	public String getDescription() {
		
		return "File Based Identity Provider";
	}

}
