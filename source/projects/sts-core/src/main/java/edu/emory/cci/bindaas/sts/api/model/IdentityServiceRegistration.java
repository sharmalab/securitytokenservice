package edu.emory.cci.bindaas.sts.api.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

public class IdentityServiceRegistration {
	@Expose private String id;
	@Expose private Date created;
	@Expose private JsonObject configuration;
	@Expose private String identityProviderId;
	@Expose private String description;
	
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
