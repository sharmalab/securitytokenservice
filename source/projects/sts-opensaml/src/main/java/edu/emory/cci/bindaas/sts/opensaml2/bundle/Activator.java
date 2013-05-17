package edu.emory.cci.bindaas.sts.opensaml2.bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.sts.opensaml2.core.TokenGenerator;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private Log log = LogFactory.getLog(getClass());
	public void start(BundleContext arg0) throws Exception {
		
		context = arg0;
		log.debug("--- Started ----");
		TokenGenerator.main(null);
	}

	
	public void stop(BundleContext arg0) throws Exception {
		
		
	}
	
	public static BundleContext getContext()
	{
		return context;
	}

}
