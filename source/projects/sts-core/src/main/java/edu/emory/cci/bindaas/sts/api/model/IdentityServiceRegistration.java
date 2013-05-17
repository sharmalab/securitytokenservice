package edu.emory.cci.bindaas.sts.api.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class IdentityServiceRegistration implements Serializable{
	
	@Override
	public boolean equals(Object obj) {
		
		return obj instanceof IdentityServiceRegistration && ( (IdentityServiceRegistration) obj ).id.equals(this.id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	private static final long serialVersionUID = 3922030034812135781L;
	@Expose private String id;
	@Expose private Date created;
	@Expose private JsonObject configuration;
	@Expose private String identityProviderId;
	@Expose private String description;
	@Expose private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentityProviderId() {
		return identityProviderId;
	}
	public void setIdentityProviderId(String identityProviderId) {
		this.identityProviderId = identityProviderId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public JsonObject getConfiguration() {
		return configuration;
	}
	public void setConfiguration(JsonObject configuration) {
		this.configuration = configuration;
	}

}
