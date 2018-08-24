package org.rakshak.idp.ldap_csm.api;

import java.util.Set;

import org.rakshak.core.api.model.Group;
import org.rakshak.idp.ldap_csm.ext.CSMConfiguration;

public interface ICSMConfigurationHandler {
	public Set<String> getGroups(String username,
			CSMConfiguration csmConfiguration) throws Exception;

	public Set<Group> getGroups(CSMConfiguration csmConfiguration)
			throws Exception;

}
