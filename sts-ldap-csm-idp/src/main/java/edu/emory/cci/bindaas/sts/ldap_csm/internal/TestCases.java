package edu.emory.cci.bindaas.sts.ldap_csm.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.api.model.SecureToken;
import edu.emory.cci.bindaas.sts.ldap_csm.bundle.Activator;
import edu.emory.cci.bindaas.sts.ldap_csm.ext.CSMConfiguration;
import edu.emory.cci.bindaas.sts.ldap_csm.ext.LDAPConfiguration;
import edu.emory.cci.bindaas.sts.ldap_csm.ext.LdapCsmIdentityProvider;
import edu.emory.cci.bindaas.sts.ldap_csm.ext.LdapCsmIdentityServiceConfiguration;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.GSONUtil;

public class TestCases {

private ServiceTracker<IManagerService, IManagerService> serviceTracker;
	
	public TestCases()
	{
		this.serviceTracker = new ServiceTracker<IManagerService, IManagerService>(Activator.getContext(), IManagerService.class, null);
		this.serviceTracker.open();
	}
	
	public IManagerService getManagerServiceInstance()
	{
		
		return this.serviceTracker.getService();
	}
	
	private LDAPConfiguration getLDAPConfiguration()
	{
		LDAPConfiguration configuration = new LDAPConfiguration();
		configuration.setLdapUrl("ldap://10.28.163.89:1389");
		configuration.setDnTemplate("cn=%s,ou=Users,dc=cancerimagingarchive,dc=net");
		configuration.setSearchUsersLdapQuery("ou=Users,dc=cancerimagingarchive,dc=net");
		configuration.setTestPassword(null); // intentionally left null
		configuration.setTestUsername(null); // intentionally left null
		return configuration;
	}
	
	private CSMConfiguration getCSMConfiguration()
	{
		CSMConfiguration csmConfiguration = new CSMConfiguration();
		csmConfiguration.setJdbcDriver("com.mysql.jdbc.Driver");
		csmConfiguration.setJdbcUrl("jdbc:mysql://10.28.163.194:3306/ncia");
		csmConfiguration.setUsername("nciaadmin");
		csmConfiguration.setPassword("nciA#112");
		return csmConfiguration;
	}
	
	@Test
	public void issueValidateToken()
	{
		IManagerService managerService = getManagerServiceInstance();
		String identityProviderId = LdapCsmIdentityProvider.class.getName();
		String name = "TCIA-CSM Service";
		String description = "TCIA-CSM Service";
		LdapCsmIdentityServiceConfiguration fileConfig = new LdapCsmIdentityServiceConfiguration();
		
		fileConfig.setTokenLifetime(200);
		fileConfig.setIssuerName("Emory University - Department of Biomedical Informatics");
		fileConfig.setCsmConfiguration(getCSMConfiguration());
		fileConfig.setLdapConfiguration(getLDAPConfiguration());
		
		
		JsonObject configuration = GSONUtil.getGSONInstance().toJsonTree(fileConfig).getAsJsonObject();
		try {
			IdentityServiceRegistration serviceReg = managerService.registerService(identityProviderId, name, description, configuration);
			System.out.println(serviceReg.getConfiguration());
			System.out.println(serviceReg.getId());
			IIdentityService identityService = managerService.getService(serviceReg.getId());
			String token = identityService.issueToken(new Credential("nadirsagha", "tapori34315"), "http://tcia.org").getContent();
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
