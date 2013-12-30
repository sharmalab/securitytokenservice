package edu.emory.cci.bindaas.sts.ldap.ext;

import edu.emory.cci.bindaas.sts.api.IIdentityProvider;
import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.service.IConfigurationManagerService;
import edu.emory.cci.bindaas.sts.util.JDBCDriverRegistry;

public class LdapIdentityProvider implements IIdentityProvider {

	private IConfigurationManagerService configurationManager;
	private String keyPairFileName ;
	private JDBCDriverRegistry jdbcDriverRegistry;
	
	
	
	
	public JDBCDriverRegistry getJdbcDriverRegistry() {
		return jdbcDriverRegistry;
	}

	public void setJdbcDriverRegistry(JDBCDriverRegistry jdbcDriverRegistry) {
		this.jdbcDriverRegistry = jdbcDriverRegistry;
	}

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
		LdapIdentityService service = new LdapIdentityService();
		service.setConfigurationManager(this.configurationManager);
		service.setKeyPairFilename(this.keyPairFileName);
		service.setup(serviceReg);
		return service;
	}

	public String getDescription() {
		
		return "LDAP/CSM Identity Provider";
	}

}
