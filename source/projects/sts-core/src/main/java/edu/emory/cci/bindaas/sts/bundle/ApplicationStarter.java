package edu.emory.cci.bindaas.sts.bundle;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleContext;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceAlreadyExistException;
import edu.emory.cci.bindaas.sts.ext.FileBasedIdentityProvider;
import edu.emory.cci.bindaas.sts.ext.FileBasedIdentityServiceConfiguration;
import edu.emory.cci.bindaas.sts.internal.conf.SystemConfiguration;
import edu.emory.cci.bindaas.sts.rest.api.ISecurityTokenServiceCore;
import edu.emory.cci.bindaas.sts.rest.impl.SecurityTokenServiceCoreImpl;
import edu.emory.cci.bindaas.sts.service.IConfigurationManagerService;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.DynamicObject;
import edu.emory.cci.bindaas.sts.util.GSONUtil;

public class ApplicationStarter {

	private SecurityTokenServiceCoreImpl securityTokenService;
	private DynamicObject<SystemConfiguration> systemConfiguration;
	private  static final String SYSTEM_CONFIG_AREA = "system";
	private IConfigurationManagerService configurationManager;
	private SystemConfiguration defaultSystemConfiguration;
	private IManagerService managementService;
	private FileBasedIdentityServiceConfiguration adminFileBasedIdentityConfiguration;
	private FileBasedIdentityProvider fileBasedIdentityProvider;
	
	
	
	
	public FileBasedIdentityProvider getFileBasedIdentityProvider() {
		return fileBasedIdentityProvider;
	}

	public void setFileBasedIdentityProvider(
			FileBasedIdentityProvider fileBasedIdentityProvider) {
		this.fileBasedIdentityProvider = fileBasedIdentityProvider;
	}

	public FileBasedIdentityServiceConfiguration getAdminFileBasedIdentityConfiguration() {
		return adminFileBasedIdentityConfiguration;
	}

	public void setAdminFileBasedIdentityConfiguration(
			FileBasedIdentityServiceConfiguration adminFileBasedIdentityConfiguration) {
		this.adminFileBasedIdentityConfiguration = adminFileBasedIdentityConfiguration;
	}

	public IManagerService getManagementService() {
		return managementService;
	}

	public void setManagementService(IManagerService managementService) {
		this.managementService = managementService;
	}

	public DynamicObject<SystemConfiguration> getSystemConfiguration() {
		return systemConfiguration;
	}

	public void setSystemConfiguration(
			DynamicObject<SystemConfiguration> systemConfiguration) {
		this.systemConfiguration = systemConfiguration;
	}

	public IConfigurationManagerService getConfigurationManager() {
		return configurationManager;
	}

	public void setConfigurationManager(
			IConfigurationManagerService configurationManager) {
		this.configurationManager = configurationManager;
	}

	public SecurityTokenServiceCoreImpl getSecurityTokenService() {
		return securityTokenService;
	}

	public void setSecurityTokenService(
			SecurityTokenServiceCoreImpl securityTokenService) {
		this.securityTokenService = securityTokenService;
	}


	
	public SystemConfiguration getDefaultSystemConfiguration() {
		return defaultSystemConfiguration;
	}

	public void setDefaultSystemConfiguration(
			SystemConfiguration defaultSystemConfiguration) {
		this.defaultSystemConfiguration = defaultSystemConfiguration;
	}

	private void createDefaultAdminIdentityService(String name) throws IdentityProviderException
	{
		
		
		JsonObject configuration = GSONUtil.getGSONInstance().toJsonTree(adminFileBasedIdentityConfiguration).getAsJsonObject();
		try {
				managementService.registerService( fileBasedIdentityProvider , name, "Default Identity Service for Admin Access", configuration , true);
			
		} catch (IdentityServiceAlreadyExistException e) {
			// do nothing
			
		}
		
	}
	public void startup() throws Exception
	{
		systemConfiguration = new DynamicObject<SystemConfiguration>(SYSTEM_CONFIG_AREA, "system", defaultSystemConfiguration, Activator.getContext(), configurationManager);
		
		String host = systemConfiguration.getObject().getStsHost();
		Integer port = systemConfiguration.getObject().getStsPort();
		String protocol = systemConfiguration.getObject().getStsProtocol();
		
		createDefaultAdminIdentityService(systemConfiguration.getObject().getAdminIdentityService());
		String publishUrl = String.format("%s://%s:%s/securityTokenService", protocol , host , port);
		BundleContext context = Activator.getContext();
		
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("edu.emory.cci.sts.cxf.service.name", "Security Token Service");
		props.put("edu.emory.cci.sts.cxf.service.address",  publishUrl );
		context.registerService(ISecurityTokenServiceCore.class.getName(), securityTokenService, props);
	}
}
