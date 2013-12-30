package test;

import java.sql.Driver;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.emory.cci.bindaas.sts.ldap_csm.ext.CSMConfiguration;
import edu.emory.cci.bindaas.sts.ldap_csm.ext.DefaultCSMConfigurationHandler;
import edu.emory.cci.bindaas.sts.util.JDBCDriverRegistry;

public class TestCSMConfiguration {

	public static void main(String[] args) throws Exception {
		CSMConfiguration csmConfiguration = new CSMConfiguration();
		csmConfiguration.setJdbcDriver("com.mysql.jdbc.Driver");
		csmConfiguration.setJdbcUrl("jdbc:mysql://10.28.163.194:3306/ncia");
		csmConfiguration.setUsername("nciaadmin");
		csmConfiguration.setPassword("nciA#112");
		
		
		String fetchAllGroupsSQL = "select C.group_name , B.login_name from csm_user_group A inner join csm_user B on A.user_id = B.user_id inner join csm_group C on A.group_id = C.group_id";
		String fetchUserGroupSQL = "select C.group_name from csm_user_group A inner join csm_user B on A.user_id = B.user_id inner join csm_group C on A.group_id = C.group_id where B.login_name = ?";
		
		JDBCDriverRegistry jdbcDriverRegistry = new JDBCDriverRegistry();
		Table<String,String,Driver> table = HashBasedTable.create();
		jdbcDriverRegistry.setDriverRegistry(table);
		table.put("com.mysql.jdbc.Driver", "5.0.8", new com.mysql.jdbc.Driver());
		
		
		DefaultCSMConfigurationHandler csmHandler = new DefaultCSMConfigurationHandler();
		csmHandler.setFetchAllGroupsSQL(fetchAllGroupsSQL);
		csmHandler.setFetchUserGroupSQL(fetchUserGroupSQL);
		csmHandler.setJdbcDriverRegistry(jdbcDriverRegistry);
		
		CSMConfiguration.testCSMConnection(csmConfiguration, jdbcDriverRegistry);
		
		System.out.println(csmHandler.getGroups(csmConfiguration));
		System.out.println(csmHandler.getGroups("smoore", csmConfiguration));
		
		
	}
}
