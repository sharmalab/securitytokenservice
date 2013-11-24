package edu.emory.cci.bindaas.sts.internal.web;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import edu.emory.cci.bindaas.sts.bundle.Activator;
import edu.emory.cci.bindaas.sts.internal.web.login.LoginFilter;
/**
 * WebApplicationLauncher is the base class that registers GeneralServlets from all WebModules.
 * @author nadir
 *
 */
public class WebApplicationLauncher {

	private final static String WEBCONTENT_DIRECTORY=  "/webcontent";
	private List<WebModule> listOfAppInitializers;
	private LoginFilter loginFilter;
	private Log log = LogFactory.getLog(getClass());
	
	
	
	public LoginFilter getLoginFilter() {
		return loginFilter;
	}


	public void setLoginFilter(LoginFilter loginFilter) {
		this.loginFilter = loginFilter;
	}


	public List<WebModule> getListOfAppInitializers() {
		return listOfAppInitializers;
	}


	public void setListOfAppInitializers(
			List<WebModule> listOfAppInitializers) {
		this.listOfAppInitializers = listOfAppInitializers;
	}


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
			for(WebModule appInitializer : this.listOfAppInitializers)
			{
				
				appInitializer.init(httpService, defaultContext);
			}
			
			
			httpService.registerResources( WEBCONTENT_DIRECTORY, WEBCONTENT_DIRECTORY, defaultContext);
			ExtHttpService.class.cast(httpService).registerFilter(loginFilter	, loginFilter.getServletUrl(), null, 0, defaultContext);
		}catch(Exception e)
		{
			log.fatal("ApplicationStarter did not initialize",e);
		}
	}

}
