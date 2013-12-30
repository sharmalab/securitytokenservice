package edu.emory.cci.bindaas.sts.ldap_csm.api;

import java.util.Set;

import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.ldap_csm.ext.CSMConfiguration;

public interface ICSMConfigurationHandler {
	public Set<String> getGroups(String username,
			CSMConfiguration csmConfiguration) throws Exception;

	public Set<Group> getGroups(CSMConfiguration csmConfiguration)
			throws Exception;

}
