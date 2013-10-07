package edu.emory.cci.bindaas.sts.internal.web;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import edu.emory.cci.bindaas.sts.bundle.Activator;

public class WebApplicationLauncher {

	private final static String WEBCONTENT_DIRECTORY=  "/webcontent";
	private List<WebAppInitializer> listOfAppInitializers;
	private Log log = LogFactory.getLog(getClass());
	
	public void init() throws Exception
	{
		// register all servlets,filters,etc
		final BundleContext context = Activator.getContext();
		ServiceTracker<HttpService,HttpService> serviceTracker = new ServiceTracker<HttpService,HttpService>(context, HttpService.class, new ServiceTrackerCustomizer<HttpService, HttpService>() {

			
			public HttpService addingService(ServiceReference<HttpService> srf) {
				HttpService httpService = context.getService(srf); 
				registerResources(httpService);
				return httpService;
			}

			
			public void modifiedService(ServiceReference<HttpService> arg0,
					HttpService arg1) {
			}

			
			public void removedService(ServiceReference<HttpService> arg0,
					HttpService arg1) {
				}
		});
		
		serviceTracker.open();
		
		
	}
	
	
	private void registerResources(HttpService httpService) 
	{
		try{
			HttpContext defaultContext = httpService.createDefaultHttpContext();
			for(WebAppInitializer appInitializer : this.listOfAppInitializers)
			{
				
				appInitializer.init(httpService, defaultContext);
			}
			
			
			httpService.registerResources(WEBCONTENT_DIRECTORY, WEBCONTENT_DIRECTORY, defaultContext);
			
		}catch(Exception e)
		{
			log.fatal("ApplicationStarter did not initialize",e);
		}
	}

}
