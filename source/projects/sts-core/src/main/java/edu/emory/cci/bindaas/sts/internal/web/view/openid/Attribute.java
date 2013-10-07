package edu.emory.cci.bindaas.sts.internal.web.view.openid;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.api.model.User.Gender;

public enum Attribute {
	

	EMAIL("Email Address" , "email" , "" , new ValueExtractor(){

		public String extractValue(User user) {
			return user.getEmail();
		}}) , 
	FULLNAME("Full Name" , "fullname" , "", new ValueExtractor(){

		public String extractValue(User user) {
			return String.format("%s %s", user.getFirstName() , user.getLastName());
		}}) , 
	DOB("Date of Birth" , "dob" , "", new ValueExtractor(){
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		public String extractValue(User user) {
			return dateFormat.format(user.getDob());
		}}) ,
	
	GENDER("Gender" , "gender" , "", new ValueExtractor(){

		public String extractValue(User user) {
				Gender gender = user.getGender()!=null ? user.getGender() : Gender.UNKNOWN;
				return gender.toString();
		}}) ,
	
	NICKNAME("Nickname" , "nickname" , "", new ValueExtractor(){

		public String extractValue(User user) {
			String nickname = user.getNickname() !=null?user.getNickname() : user.getFirstName();
			return nickname;
		}}) ,
	POSTCODE("Postal Code" , "postcode" , "", new ValueExtractor(){

		public String extractValue(User user) {
			return user.getPostcode();
		}}) ;
	
	private  static Map<String,Attribute> sregLookupTable = new HashMap<String, Attribute>();
	private  static Map<String,Attribute> axLookupTable = new HashMap<String, Attribute>();
	
	
	Attribute (String commonName , String sregName , String axSchema , ValueExtractor valueExtractor)
	{
		this.axSchema = axSchema;
		this.sregName = sregName;
		this.commonName = commonName;
		this.valueExtractor = valueExtractor;
		
		updateSregLookup(sregName, this);
		updateAxLookup(axSchema, this);
	}
	
	
	private static void updateSregLookup(String sregName , Attribute attr)
	{
		sregLookupTable.put(sregName, attr);
	}
	
	private static void updateAxLookup(String axSchema , Attribute attr)
	{
		axLookupTable.put(axSchema, attr);
	}
	
	
	public static Attribute lookupBySreg(String sreg)
	{
		return sregLookupTable.get(sreg);
	}
	
	public static Attribute lookupByAxSchema(String axSchema)
	{
		return axLookupTable.get(axSchema);
	}
	
	private String axSchema;
	private String sregName;
	private String commonName;
	private ValueExtractor valueExtractor;
	
	
	public String getAxSchema() {
		return axSchema;
	}
	public void setAxSchema(String axSchema) {
		this.axSchema = axSchema;
	}
	public String getSregName() {
		return sregName;
	}
	public void setSregName(String sregName) {
		this.sregName = sregName;
	}
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String extractValue(User user)
	{
		return valueExtractor.extractValue(user);
	}
	
	public static interface ValueExtractor {
		public String extractValue(User user);
		
	}
	
	
	
}
