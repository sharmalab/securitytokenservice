package edu.emory.cci.bindaas.sts.cagrid.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	public void start(BundleContext arg0) throws Exception {
		
		context = arg0;
		
		
		
	}

	
	public void stop(BundleContext arg0) throws Exception {
		
		
	}
	
	public static BundleContext getContext()
	{
		
		return context;
	}

}
