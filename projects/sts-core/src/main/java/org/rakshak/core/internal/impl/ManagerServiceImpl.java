package org.rakshak.core.internal.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.rakshak.core.api.IDGenerator;
import org.rakshak.core.api.IIdentityProvider;
import org.rakshak.core.api.IIdentityService;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.exception.IdentityServiceAlreadyExistException;
import org.rakshak.core.api.exception.IdentityServiceNotFoundException;
import org.rakshak.core.api.model.IdentityServiceRegistration;
import org.rakshak.core.bundle.Activator;
import org.rakshak.core.service.IConfigurationManagerService;
import org.rakshak.core.service.IManagerService;
import org.rakshak.core.util.IPartialMap;
import org.rakshak.core.util.PersistentHashMap;

import com.google.gson.JsonObject;

public class ManagerServiceImpl  implements IManagerService{

	private String serviceRegistrationFile;
	private IDGenerator idGenerator;
	
	
	public IDGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IDGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public String getServiceRegistrationFile() {
		return serviceRegistrationFile;
	}

	public void setServiceRegistrationFile(String serviceRegistrationFile) {
		this.serviceRegistrationFile = serviceRegistrationFile;
	}

	private IPartialMap<IdentityServiceRegistration> serviceRegistrationMap;
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
		idGenerator = new URLFriendlyIDGenerator();
		serviceRegistrationMap = new PersistentHashMap<IdentityServiceRegistration>(new File(serviceRegistrationFile) ,  IdentityServiceRegistration.class);
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
	
	public synchronized IdentityServiceRegistration registerService(
			String identityProviderId, String name , String description,
			JsonObject configuration , boolean overwrite) throws IdentityProviderException, IdentityServiceAlreadyExistException {
		
		String id = idGenerator.generateID(name);
		IdentityServiceRegistration serviceRegistration = serviceRegistrationMap.get(id);
		if(!overwrite && serviceRegistration!=null)
			throw new IdentityServiceAlreadyExistException(id);
		
		// lookup correct IdentityProvider
		IIdentityProvider identityProvider = lookupIdentityProvider(identityProviderId);
		if(identityProvider!=null)
		{
			// delegate creation of the service
			IdentityServiceRegistration serviceReg = new IdentityServiceRegistration();
			serviceReg.setConfiguration(configuration);
			serviceReg.setCreated(new Date());
			serviceReg.setDescription(description);
			serviceReg.setId(id);
			serviceReg.setName(name);
			serviceReg.setIdentityProviderId(identityProviderId);
			IIdentityService identityService = identityProvider.createService(serviceReg);
			// persist registration object
			serviceRegistrationMap.put(identityService.getRegistrationInfo().getId(), identityService.getRegistrationInfo());
			addService(identityService);
			// return registration object
			log.info("Identity Service [" + serviceReg.getId() + "] Created");
			return identityService.getRegistrationInfo();
			
		}
		else
		{
			throw new IdentityProviderException(identityProviderId , "IdentityProvider not found");
		}

	}

	public synchronized IdentityServiceRegistration removeService(String id)
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

	public List<IdentityServiceRegistration> getAllService() {

		return serviceRegistrationMap.values();
	}

	public List<IIdentityProvider> getIdentityProviders() {
		IIdentityProvider[] availableIdentityProviders = new IIdentityProvider[identityProviderTracker.size()];
		availableIdentityProviders = identityProviderTracker.getServices(availableIdentityProviders);
		return Arrays.asList(availableIdentityProviders);
	}

	public IdentityServiceRegistration registerService(
			IIdentityProvider identityProvider, String name,
			String description, JsonObject configuration, boolean overwrite)
			throws IdentityProviderException,
			IdentityServiceAlreadyExistException {
		String id = idGenerator.generateID(name);
		IdentityServiceRegistration serviceRegistration = serviceRegistrationMap.get(id);
		if(!overwrite && serviceRegistration!=null)
			throw new IdentityServiceAlreadyExistException(id);
		
		// delegate creation of the service
		IdentityServiceRegistration serviceReg = new IdentityServiceRegistration();
		serviceReg.setConfiguration(configuration);
		serviceReg.setCreated(new Date());
		serviceReg.setDescription(description);
		serviceReg.setId(id);
		serviceReg.setName(name);
		serviceReg.setIdentityProviderId(identityProvider.getClass().getName());
		IIdentityService identityService = identityProvider.createService(serviceReg);
		// persist registration object
		serviceRegistrationMap.put(identityService.getRegistrationInfo().getId(), identityService.getRegistrationInfo());
		addService(identityService);
		// return registration object
		log.info("Identity Service [" + serviceReg.getId() + "] Created");
		return identityService.getRegistrationInfo();
		
	}

	

}
