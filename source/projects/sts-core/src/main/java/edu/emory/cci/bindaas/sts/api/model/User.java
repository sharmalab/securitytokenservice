package edu.emory.cci.bindaas.sts.api.model;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class User {
	@Expose private String name;
	@Expose private Map<String,String> properties;
	@Expose private List<Group> groups;
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
	public List<Group> getGroups() {
		return groups;
	}
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
}
