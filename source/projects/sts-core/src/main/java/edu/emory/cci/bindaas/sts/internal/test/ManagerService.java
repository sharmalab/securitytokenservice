package edu.emory.cci.bindaas.sts.internal.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.api.model.SecureToken;
import edu.emory.cci.bindaas.sts.bundle.Activator;
import edu.emory.cci.bindaas.sts.ext.FileBasedIdentityProvider;
import edu.emory.cci.bindaas.sts.ext.FileBasedIdentityServiceConfiguration;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.GSONUtil;

public class ManagerService {

	private ServiceTracker<IManagerService, IManagerService> serviceTracker;
	
	public ManagerService()
	{
		this.serviceTracker = new ServiceTracker<IManagerService, IManagerService>(Activator.getContext(), IManagerService.class, null);
		this.serviceTracker.open();
	}
	
	public IManagerService getManagerServiceInstance()
	{
		
		return this.serviceTracker.getService();
	}
	
	
	@Test
	public void createIdentityServiceConfiguration()
	{
		IManagerService managerService = getManagerServiceInstance();
		String identityProviderId = FileBasedIdentityProvider.class.getName();
		String name = "Bootstrap";
		String description = "Bootstrap service";
		FileBasedIdentityServiceConfiguration fileConfig = new FileBasedIdentityServiceConfiguration();
		fileConfig.setTokenLifetime(200);
		fileConfig.setGroupStore(new HashMap<String, List<String>>());
		fileConfig.setUserStore(new HashMap<String, String>());
		fileConfig.getUserStore().put("admin", "password");
		fileConfig.getUserStore().put("nadir", "password");
		List<String> usr = new ArrayList<String>();
		usr.add("nadir");
		
		fileConfig.getGroupStore().put("administrator", usr);
		fileConfig.getGroupStore().put("users", usr);
		
		JsonObject configuration = GSONUtil.getGSONInstance().toJsonTree(fileConfig).getAsJsonObject();
		try {
			managerService.registerService(identityProviderId, name, description, configuration);
		} catch (IdentityProviderException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void issueValidateToken()
	{
		IManagerService managerService = getManagerServiceInstance();
		String identityProviderId = FileBasedIdentityProvider.class.getName();
		String name = "Test Identity Service";
		String description = "Test Identity Service";
		FileBasedIdentityServiceConfiguration fileConfig = new FileBasedIdentityServiceConfiguration();
		fileConfig.setTokenLifetime(200);
		fileConfig.setGroupStore(new HashMap<String, List<String>>());
		fileConfig.setUserStore(new HashMap<String, String>());
		fileConfig.getUserStore().put("admin", "password");
		fileConfig.getUserStore().put("nadir", "password");
		List<String> usr = new ArrayList<String>();
		usr.add("nadir");
		
		fileConfig.getGroupStore().put("administrator", usr);
		fileConfig.getGroupStore().put("users", usr);
		
		JsonObject configuration = GSONUtil.getGSONInstance().toJsonTree(fileConfig).getAsJsonObject();
		try {
			IdentityServiceRegistration serviceReg = managerService.registerService(identityProviderId, name, description, configuration);
			IIdentityService identityService = managerService.getService(serviceReg.getId());
			String token = identityService.issueToken(new Credential("nadir", "password"), "http://someapplication.org").getContent();
			System.out.println(token);
			assertNotNull(token);
			
			// validate token
			boolean stat = identityService.validateToken(new SecureToken( token ));
			assertTrue(stat);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
}
