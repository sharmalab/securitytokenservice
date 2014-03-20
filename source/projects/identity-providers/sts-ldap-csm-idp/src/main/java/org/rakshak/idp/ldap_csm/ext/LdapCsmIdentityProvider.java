package org.rakshak.idp.ldap_csm.ext;

import org.rakshak.core.api.IIdentityProvider;
import org.rakshak.core.api.IIdentityService;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.model.IdentityServiceRegistration;
import org.rakshak.core.service.IConfigurationManagerService;
import org.rakshak.core.util.JDBCDriverRegistry;
import org.rakshak.idp.ldap_csm.api.ICSMConfigurationHandler;
import org.rakshak.idp.ldap_csm.api.ILDAPConfigurationHandler;

public class LdapCsmIdentityProvider implements IIdentityProvider {

	private IConfigurationManagerService configurationManager;
	private String keyPairFileName ;
	private JDBCDriverRegistry jdbcDriverRegistry;
	private ILDAPConfigurationHandler ldapConfigHandler;
	private ICSMConfigurationHandler csmConfigurationHandler;
	
	
	
	public JDBCDriverRegistry getJdbcDriverRegistry() {
		return jdbcDriverRegistry;
	}

	public void setJdbcDriverRegistry(JDBCDriverRegistry jdbcDriverRegistry) {
		this.jdbcDriverRegistry = jdbcDriverRegistry;
	}

	public ILDAPConfigurationHandler getLdapConfigHandler() {
		return ldapConfigHandler;
	}

	public void setLdapConfigHandler(ILDAPConfigurationHandler ldapConfigHandler) {
		this.ldapConfigHandler = ldapConfigHandler;
	}

	public ICSMConfigurationHandler getCsmConfigurationHandler() {
		return csmConfigurationHandler;
	}

	public void setCsmConfigurationHandler(
			ICSMConfigurationHandler csmConfigurationHandler) {
		this.csmConfigurationHandler = csmConfigurationHandler;
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
		LdapCsmIdentityService service = new LdapCsmIdentityService();
		service.setConfigurationManager(this.configurationManager);
		service.setKeyPairFilename(this.keyPairFileName);
		service.setCsmConfigurationHandler(csmConfigurationHandler);
		service.setLdapConfigHandler(ldapConfigHandler);
		service.setJdbcDriverRegistry(jdbcDriverRegistry);
		service.setup(serviceReg);
		return service;
	}

	public String getDescription() {
		
		return "LDAP/CSM Identity Provider";
	}

}
