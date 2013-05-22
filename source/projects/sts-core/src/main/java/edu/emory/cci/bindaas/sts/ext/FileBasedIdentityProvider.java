package edu.emory.cci.bindaas.sts.ext;

import edu.emory.cci.bindaas.sts.api.IIdentityProvider;
import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.internal.api.IConfigurationManager;

public class FileBasedIdentityProvider implements IIdentityProvider {

	private IConfigurationManager configurationManager;
	private String keyPairFileName ;
	
	public String getKeyPairFileName() {
		return keyPairFileName;
	}

	public void setKeyPairFileName(String keyPairFileName) {
		this.keyPairFileName = keyPairFileName;
	}

	public IConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public void setConfigurationManager(IConfigurationManager configurationManager) {
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

}
