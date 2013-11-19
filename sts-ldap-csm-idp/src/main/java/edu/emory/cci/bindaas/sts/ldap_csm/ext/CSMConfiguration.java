package edu.emory.cci.bindaas.sts.ldap_csm.ext;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.ldap_csm.api.ICSMConfigurationHandler;
import edu.emory.cci.bindaas.sts.util.JDBCDriverRegistry;

public class CSMConfiguration {
	@Expose private String username;
	@Expose private String password;
	@Expose private String jdbcUrl;
	@Expose private String jdbcDriver;
	
	private static Log log = LogFactory.getLog(CSMConfiguration.class);
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}
	public String getJdbcDriver() {
		return jdbcDriver;
	}
	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
		
	}
	
	public static Set<String> getGroups(String username , CSMConfiguration csmConfiguration , ICSMConfigurationHandler csmConfigurationHandler) throws Exception
	{
		
		return csmConfigurationHandler.getGroups(username, csmConfiguration) ;
	}
	
	public static Set<Group> getGroups(CSMConfiguration csmConfiguration , ICSMConfigurationHandler csmConfigurationHandler) throws Exception
	{
		return csmConfigurationHandler.getGroups(csmConfiguration) ;
	}
	
	
	/**
	 * checks if csmConfiguration is valid
	 * @param csmConfiguration
	 * @throws Exception
	 */
	public static void testCSMConnection(CSMConfiguration csmConfiguration , JDBCDriverRegistry jdbcDriverRegistry) throws Exception
	{
		Connection conn = null;
		try{
		
			Driver driver = jdbcDriverRegistry.getDriverByName(csmConfiguration.getJdbcDriver());
			Properties connectionProps = new Properties();
			connectionProps.put("user", csmConfiguration.getUsername());
			connectionProps.put("password", csmConfiguration.getPassword());
			conn = driver.connect(csmConfiguration.getJdbcUrl(), connectionProps);
			DatabaseMetaData metadata = conn.getMetaData();
			log.debug("Successfully connected to [" + metadata.getDatabaseProductName() + "] version [" + metadata.getDatabaseProductVersion() + "]");
			System.out.println("Successfully connected to [" + metadata.getDatabaseProductName() + "] version [" + metadata.getDatabaseProductVersion() + "]");
		}
		catch(NullPointerException e)
		{
			throw new Exception("Mandatory field [username|password|jdbcUrl|jdbcDriver] not specified");
		}
		catch(SQLException e)
		{
			throw new Exception(e);
		}
		catch(Exception e)
		{
			throw e;
		}
		finally{
			if(conn!=null)
			{
				conn.close();
			}
		}
	}
}
