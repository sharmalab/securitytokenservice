package edu.emory.cci.bindaas.sts.api.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class User {
	@Expose private String name; // username
	@Expose private Map<String,String> attributes; // other attributes
	@Expose private String email; 
	@Expose private String firstName;
	@Expose private String lastName;
	@Expose private Date dob; //  YYYY-MM-DD
	@Expose private String nickname;
	@Expose private String postcode;
	@Expose private Gender gender;
	@Expose private List<Group> groups;	
	
	public Map<String,String> getSregAttributes()
	{
		return null; // TODO: implemented
	}
	
	public Map<String,String> getAxAttributes(Map<String,String> namespaceMapping)
	{
		return null; // TODO: implemented
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Date getDob() {
		return dob;
	}
	public void setDob(Date dob) {
		this.dob = dob;
	}
	
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public Gender getGender() {
		return gender;
	}
	public void setGender(Gender gender) {
		this.gender = gender;
	}
	public static enum Gender { M ,F  , UNKNOWN }
	

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Group> getGroups() {
		return groups;
	}
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
}
