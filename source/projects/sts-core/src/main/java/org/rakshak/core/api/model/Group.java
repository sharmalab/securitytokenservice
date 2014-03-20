package org.rakshak.core.api.model;

import java.util.Map;
import java.util.Set;

import org.rakshak.core.util.GSONUtil;

import com.google.gson.annotations.Expose;

public class Group {

	@Expose private String name;
	@Expose private Map<String,String> properties;
	@Expose private Set<String> users;
	@Expose private String description;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	public Set<String> getUsers() {
		return users;
	}
	public void setUsers(Set<String> users) {
		this.users = users;
	}
	
	public int hashCode()
	{
		return name.hashCode();
	}
	
	public boolean equals(Object group)
	{
		if(group instanceof Group)
		{
			return Group.class.cast(group).name.equals(this.name);
		}
		
		return false;
	}
	
	public String toString(){
		return GSONUtil.getGSONInstance().toJson(this);
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
