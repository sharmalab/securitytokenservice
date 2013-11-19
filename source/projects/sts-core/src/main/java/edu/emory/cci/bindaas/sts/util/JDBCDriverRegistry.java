package edu.emory.cci.bindaas.sts.util;

import java.sql.Driver;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.sts.bundle.Activator;

public class JDBCDriverRegistry implements  ServiceTrackerCustomizer<Driver, Driver>{

	private ServiceTracker<Driver, Driver> jdbcDriverTracker;
	
	private Log log = LogFactory.getLog(getClass());
	private Table<String, String , Driver> driverRegistry;

	public synchronized Driver getDriverByName(String clazz)
	{
		if(driverRegistry.containsRow(clazz))
		{
			Collection<Driver>  drivers = driverRegistry.row(clazz).values(); 
			if(drivers.size() > 0)
			{
				return drivers.iterator().next();
			}
		}
		
		return null ; 
	}
	
	public synchronized Driver getDriverByNameAndVersion(String clazz , String version)
	{
		return driverRegistry.get(clazz, version) ; 
	}
	
	public void init()
	{
		driverRegistry = HashBasedTable.create();
		jdbcDriverTracker = new ServiceTracker<Driver, Driver>(Activator.getContext()	, Driver.class , this);
		jdbcDriverTracker.open();
	}

	public synchronized Driver addingService(ServiceReference<Driver> srf) {
		Driver driver = Activator.getContext().getService(srf);
		
		Object obj = srf.getProperty("info");
		String version ;
		if(obj!=null && obj instanceof JDBCDriverInfo)
		{
			log.debug("Discovered JDBC Driver :\n " + obj );
			version = JDBCDriverInfo.class.cast(obj).version;
		}
		else
		{
			log.debug("Discovered JDBC Driver [" + driver.getClass() + "]"  );
			version = "default";
		}
		
		driverRegistry.put(driver.getClass().getName(), version ,  driver);
		return  driver;
	}

	public void modifiedService(ServiceReference<Driver> srf, Driver arg1) {
			
	}

	public synchronized void removedService(ServiceReference<Driver> srf, Driver driver) {
		Object obj = srf.getProperty("info");
		String version ;
		if(obj!=null && obj instanceof JDBCDriverInfo)
		{
			log.debug("Discovered JDBC Driver :\n " + obj );
			version = JDBCDriverInfo.class.cast(obj).version;
		}
		else
		{
			log.debug("Discovered JDBC Driver [" + driver.getClass() + "]"  );
			version = "default";
		}
		
		driverRegistry.remove(driver.getClass().getName(), version);
	}
	
	
	public static class JDBCDriverInfo {
		 @Expose private Class clazz;
		 @Expose private String version;
		 @Expose private String database;
		
		 public Class getClazz() {
			return clazz;
		}
		public void setClazz(Class clazz) {
			this.clazz = clazz;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getDatabase() {
			return database;
		}
		public void setDatabase(String database) {
			this.database = database;
		}
		 
		public String toString()
		{
			return GSONUtil.getGSONInstance().toJson(this);
		}
	}


	public Table<String, String, Driver> getDriverRegistry() {
		return driverRegistry;
	}

	public void setDriverRegistry(Table<String, String, Driver> driverRegistry) {
		this.driverRegistry = driverRegistry;
	}
	
}
