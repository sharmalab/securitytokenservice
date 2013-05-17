package edu.emory.cci.bindaas.sts.ext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.sts.util.GSONUtil;

public class FileBasedIdentityServiceConfiguration {

	@Expose private Map<String,String> userStore; /** username/passwords **/
	@Expose private Map<String,List<String>> groupStore; /** groups/username **/
	@Expose private String issuerName; 
	@Expose private Integer tokenLifetime;
	
	public Integer getTokenLifetime() {
		return tokenLifetime;
	}
	public void setTokenLifetime(Integer tokenLifetime) {
		this.tokenLifetime = tokenLifetime;
	}
	public Map<String, String> getUserStore() {
		return userStore;
	}
	public void setUserStore(Map<String, String> userStore) {
		this.userStore = userStore;
	}
	public Map<String, List<String>> getGroupStore() {
		return groupStore;
	}
	public void setGroupStore(Map<String, List<String>> groupStore) {
		this.groupStore = groupStore;
	}
	
	public void validate() throws Exception /** validate all fields **/
	{
		if(userStore == null ) userStore = new HashMap<String, String>();
		if(groupStore == null ) groupStore = new HashMap<String, List<String>>();
	}
	
	public static FileBasedIdentityServiceConfiguration convert(JsonObject config)
	{
		return GSONUtil.getGSONInstance().fromJson(config,FileBasedIdentityServiceConfiguration.class);
	}
	
}
