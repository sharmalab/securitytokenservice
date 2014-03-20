package org.rakshak.junit.core;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.rakshak.junit.bundle.Activator;




/**
 * Responsible for scanning test cases in other bundles
 * @author nadir
 *
 */
public class TestCaseScanner {
	private final static String TEST_MANIFEST_HEADER_NAME = "Test-Suite";
	private Map<Long,List<Class<?>>> testCases;
	private Log log = LogFactory.getLog(getClass());
	
	
	public void init()
	{
		startScanning();
		registerCommand();
	}
	
	/**
	 * Run all test cases
	 */
	public void run()
	{
		
		for( Long bundleId : testCases.keySet())
		{
				run(bundleId);
		}
		    
	}
	
	/**
	 * Run test cases from a given bundle
	 * @param id
	 */
	public void run(Long id) {
		List<Class<?>> listClazz = testCases.get(id);
		if (listClazz != null) {
			Class<?> clzzArr[] = new Class<?>[listClazz.size()];
			Result result = JUnitCore.runClasses(listClazz.toArray(clzzArr));
			if (result.wasSuccessful()) {
				log.debug("All test-cases for bundle-id[" + id
						+ "] executed successfully");
			}
			else
			{
				log.debug(String.format("Bundle-id [%s] - [%s] Test Cases Failed !!!", id,  result.getFailureCount()));
				for(Failure failure : result.getFailures())
				{
					log.debug(failure.toString());
				}
			}
			
		} else {
			log.debug("No test-cases for bundle-id[" + id + "]");
		}

	}
	public void registerCommand()
	{
		Dictionary<String, Object> dict = new Hashtable<String, Object>();
		dict.put("osgi.command.scope", "junit");
		dict.put("osgi.command.function", new String[] {"run"});
		Activator.getContext().registerService(TestCaseScanner.class, this, dict);	
	}
	
	public void startScanning(){
		testCases = new HashMap<Long, List<Class<?>>>();
		
		BundleContext context = Activator.getContext();
		Bundle[] bundles = context.getBundles();
		
		
		for(Bundle bundle : bundles)
		{
			Object value = bundle.getHeaders().get(TEST_MANIFEST_HEADER_NAME);
			
			if(value!=null)
			{
				List<Class<?>> listOfTestCases = new ArrayList<Class<?>>(); 
				testCases.put(bundle.getBundleId() , listOfTestCases);
				
				String testClassNames = value.toString();
				try{
					String[] testClasses = testClassNames.split(",");
					for(String testClass : testClasses)
					{
						Class<?> clazz = bundle.loadClass(testClass);
						log.info("Discovered TestCase [" + testClass + "] in bundle [" + bundle.getSymbolicName() + "]");
						listOfTestCases.add(clazz);
					}
				}
				catch(Exception e)
				{
					log.warn("Some TestCases from bundle [" + bundle.getSymbolicName() + "] were not added" , e);
				}
			}
		}
		
	}
}
