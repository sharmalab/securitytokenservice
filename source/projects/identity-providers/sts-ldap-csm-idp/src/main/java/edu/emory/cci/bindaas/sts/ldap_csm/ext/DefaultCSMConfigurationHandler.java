package edu.emory.cci.bindaas.sts.ldap_csm.ext;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.ldap_csm.api.ICSMConfigurationHandler;
import edu.emory.cci.bindaas.sts.ldap_csm.bundle.Activator;
import edu.emory.cci.bindaas.sts.util.JDBCDriverRegistry;
import edu.emory.cci.bindaas.sts.util.JDBCDriverRegistry.JDBCDriverInfo;

public class DefaultCSMConfigurationHandler implements ICSMConfigurationHandler {

	private JDBCDriverRegistry jdbcDriverRegistry;
	private Log log = LogFactory.getLog(getClass());
	private String fetchAllGroupsSQL;
	private String fetchUserGroupSQL;
	
	

	public void init() throws Exception
	{
		JDBCDriverInfo info = new JDBCDriverInfo();
		info.setClazz(com.mysql.jdbc.Driver.class);
		info.setDatabase("MySQL");
		info.setVersion("5.0.8");
		
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put("info", info);
		
		Activator.getContext().registerService(Driver.class, new com.mysql.jdbc.Driver() , props);
	}
	
	
	public JDBCDriverRegistry getJdbcDriverRegistry() {
		return jdbcDriverRegistry;
	}

	public void setJdbcDriverRegistry(JDBCDriverRegistry jdbcDriverRegistry) {
		this.jdbcDriverRegistry = jdbcDriverRegistry;
	}

	public String getFetchAllGroupsSQL() {
		return fetchAllGroupsSQL;
	}

	public void setFetchAllGroupsSQL(String fetchAllGroupsSQL) {
		this.fetchAllGroupsSQL = fetchAllGroupsSQL;
	}

	public String getFetchUserGroupSQL() {
		return fetchUserGroupSQL;
	}

	public void setFetchUserGroupSQL(String fetchUserGroupSQL) {
		this.fetchUserGroupSQL = fetchUserGroupSQL;
	}

	public Set<String> getGroups(String username,
			CSMConfiguration csmConfiguration) throws Exception {
		Connection conn = null;
		try{
			Set<String> set = new HashSet<String>();
			conn = getConnection(csmConfiguration);
			PreparedStatement preparedStatement = conn.prepareStatement(fetchUserGroupSQL);
			preparedStatement.setString(1, username);
			ResultSet rs = preparedStatement.executeQuery();
			while(rs.next())
			{
				set.add(rs.getString(1));
			}
			
			return set;
		}
		catch(Exception e)
		{
			log.error(e);
			throw e;
		}
		finally{
			if(conn!=null)
			{
				conn.close();
			}
		}	
	}

	private Connection getConnection(CSMConfiguration csmConfiguration) throws Exception
	{
		try{
			
			Driver driver = jdbcDriverRegistry.getDriverByName(csmConfiguration.getJdbcDriver());
			Properties connectionProps = new Properties();
			connectionProps.put("user", csmConfiguration.getUsername());
			connectionProps.put("password", csmConfiguration.getPassword());
			return driver.connect(csmConfiguration.getJdbcUrl(), connectionProps);
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
	}
	
	
	
	public Set<Group> getGroups(CSMConfiguration csmConfiguration)
			throws Exception {
		Connection conn = null;
		try{
			Map<String,Group> map = new HashMap<String,Group>();
			conn = getConnection(csmConfiguration);
			Statement statement = conn.createStatement();
			
			ResultSet rs = statement.executeQuery(fetchAllGroupsSQL);
			while(rs.next())
			{
				String groupName = rs.getString(1);
				String user = rs.getString(2);
				if(map.containsKey(groupName))
				{
					Group group = map.get(groupName);
					group.getUsers().add(user);
				}
				else
				{
					Group group = new Group();
					group.setName(groupName);
					group.setUsers(new HashSet<String>());
					group.getUsers().add(user);
					map.put(groupName , group );
				}
			}
			
			
			return new HashSet<Group>(map.values());
		}
		catch(Exception e)
		{
			log.error(e);
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
