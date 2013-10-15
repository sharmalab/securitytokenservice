package edu.emory.cci.bindaas.sts.internal.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gson.JsonObject;

import edu.emory.cci.bindaas.sts.api.IIdentityProvider;
import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceNotFoundException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.bundle.Activator;
import edu.emory.cci.bindaas.sts.service.IConfigurationManagerService;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.PersistentHashMap;

public class ManagerServiceImpl  implements IManagerService{

	private String serviceRegistrationFile;
	
	public String getServiceRegistrationFile() {
		return serviceRegistrationFile;
	}

	public void setServiceRegistrationFile(String serviceRegistrationFile) {
		this.serviceRegistrationFile = serviceRegistrationFile;
	}

	private PersistentHashMap<String, IdentityServiceRegistration> serviceRegistrationMap;
	private ServiceTracker<IIdentityProvider, IIdentityProvider> identityProviderTracker;
	private Map<IdentityServiceRegistration, IIdentityService> serviceMap;
	private IConfigurationManagerService configurationManager;
	public IConfigurationManagerService getConfigurationManager() {
		return configurationManager;
	}

	public void setConfigurationManager(IConfigurationManagerService configurationManager) {
		this.configurationManager = configurationManager;
	}

	private Log log = LogFactory.getLog(getClass());
	
	public void init() throws Exception
	{
		serviceRegistrationMap = new PersistentHashMap<String, IdentityServiceRegistration>(new File(serviceRegistrationFile) , String.class , IdentityServiceRegistration.class);
		serviceMap = new HashMap<IdentityServiceRegistration, IIdentityService>();
		identityProviderTracker = new ServiceTracker<IIdentityProvider, IIdentityProvider>(Activator.getContext(), IIdentityProvider.class, null);
		identityProviderTracker.open();
	}
	
	private void addService(IIdentityService identityService)
	{
		serviceMap.put(identityService.getRegistrationInfo(), identityService);
	}
	
	private void remove(IdentityServiceRegistration serviceReg) throws IdentityProviderException
	{
		IIdentityService identityService = serviceMap.remove(serviceReg);
		identityService.cleanup();
	}
	
	private IIdentityProvider lookupIdentityProvider(String identityProviderId)
	{
		IIdentityProvider[] availableIdentityProviders = new IIdentityProvider[identityProviderTracker.size()];
		availableIdentityProviders = identityProviderTracker.getServices(availableIdentityProviders);
		IIdentityProvider identityProvider = null;
		for(IIdentityProvider curr : availableIdentityProviders)
		{
			if(curr.getClass().getName().equals(identityProviderId)) // lookup
			{
				identityProvider = curr;
				break;
			}
		}
		return identityProvider;
	}
	
	public IdentityServiceRegistration registerService(
			String identityProviderId, String name , String description,
			JsonObject configuration) throws IdentityProviderException {
		// lookup correct IdentityProvider
		IIdentityProvider identityProvider = lookupIdentityProvider(identityProviderId);
		if(identityProvider!=null)
		{
			// delegate creation of the service
			IdentityServiceRegistration serviceReg = new IdentityServiceRegistration();
			serviceReg.setConfiguration(configuration);
			serviceReg.setCreated(new Date());
			serviceReg.setDescription(description);
			serviceReg.setId(UUID.randomUUID().toString());
			serviceReg.setName(name);
			serviceReg.setIdentityProviderId(identityProviderId);
			IIdentityService identityService = identityProvider.createService(serviceReg);
			// persist registration object
			serviceRegistrationMap.put(identityService.getRegistrationInfo().getId(), identityService.getRegistrationInfo());
			addService(identityService);
			// return registration object
			return identityService.getRegistrationInfo();
			
		}
		else
		{
			throw new IdentityProviderException(identityProviderId , "IdentityProvider not found");
		}

	}

	public IdentityServiceRegistration removeService(String id)
			throws IdentityProviderException {
		// lookup ServiceReg corresponding to id
		IdentityServiceRegistration serviceRegistration = serviceRegistrationMap.get(id);
		if(serviceRegistration!=null)
		{
			remove(serviceRegistration);
			serviceRegistrationMap.remove(id);
			
			try {
				 // clean config area
				configurationManager.clean(id);
			} catch (IOException e) {
				throw new IdentityProviderException(id, e);
			}
			
			return serviceRegistration;
		}
		else
		{
			throw new IdentityProviderException("unknown" , "No service registered with id [" + id + "]");
		}
	}

	public IIdentityService getService(String id)
			throws IdentityProviderException, IdentityServiceNotFoundException {
				// lookup ServiceReg corresponding to id
				if( id == null) throw new IdentityServiceNotFoundException("null");
				
				IdentityServiceRegistration serviceRegistration = serviceRegistrationMap.get(id);
				if(serviceRegistration!=null)
				{
					// lazy loading. If found in cache return else initialize and return
					IIdentityService identityService = serviceMap.get(serviceRegistration);
					if(identityService!=null)
					{
						return identityService;
					}
					else
					{
						// create service and add to cache
						IIdentityProvider identityProvider = lookupIdentityProvider(serviceRegistration.getIdentityProviderId());
						if(identityProvider!=null)
						{
							try {
								identityService = identityProvider.createService(serviceRegistration);
								serviceMap.put(serviceRegistration, identityService);
								return identityService;
							} catch (IdentityProviderException e) {
								log.error(e);
								throw e;
							}
						}
						else
						{
							IdentityProviderException e = new IdentityProviderException(serviceRegistration.getIdentityProviderId() , "IdentityProvider not found");
							log.error(e);
							throw e;
						}
					}
						
					 
				}
				else
				{
					throw new IdentityServiceNotFoundException(id);
				}
		}

	

}
