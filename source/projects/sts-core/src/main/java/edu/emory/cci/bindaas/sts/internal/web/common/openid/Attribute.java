package edu.emory.cci.bindaas.sts.internal.web.common.openid;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.api.model.User.Gender;

public enum Attribute {
	

	EMAIL("Email Address" , "email" , "http://openid.net/schema/contact/internet/email" , new ValueExtractor(){

		public String extractValue(User user) {
			return user.getEmail() != null ? user.getEmail() : "UNKNOWN";
		}}) , 
	
	FULLNAME("Full Name" , "fullname" , Attribute.CUSTOM_NS + "/namePerson/fullname", new ValueExtractor(){

		public String extractValue(User user) {
			if(user.getFirstName()!=null && user.getLastName()!=null)
				return String.format("%s %s", user.getFirstName() , user.getLastName());
			else if(user.getFirstName()!=null)
			{
				return user.getFirstName();
			}
			else if(user.getLastName()!=null)
			{
				return user.getLastName();
			}
			else
			{
				return "UNKNOWN";
			}
				
		}}) , 
	DOB("Date of Birth" , "dob" , Attribute.CUSTOM_NS + "/birthDate/full", new ValueExtractor(){
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		public String extractValue(User user) {
			return user.getDob() !=null ? dateFormat.format(user.getDob()) : "UNKNOWN";
		}}) ,
	
	GENDER("Gender" , "gender" , "http://openid.net/schema/gender", new ValueExtractor(){

		public String extractValue(User user) {
				Gender gender = user.getGender()!=null ? user.getGender() : Gender.UNKNOWN;
				return gender.toString();
		}}) ,
	
	NICKNAME("Nickname/Friendly Name" , "nickname" , "http://openid.net/schema/namePerson/friendly", new ValueExtractor(){

		public String extractValue(User user) {
			String nickname = user.getNickname() !=null?user.getNickname() : user.getFirstName();
			return nickname;
		}}) ,

	FIRST_NAME("First Name" , "firstname" , "http://openid.net/schema/namePerson/first" , new ValueExtractor(){

			public String extractValue(User user) {
				return user.getFirstName()  !=null ? user.getFirstName()  : "UNKNOWN";
		}}) ,
		
	LAST_NAME("Last Name" , "lastname" , "http://openid.net/schema/namePerson/last" , new ValueExtractor(){

			public String extractValue(User user) {
				return user.getLastName() !=null ? user.getLastName()  : "UNKNOWN";
		}}) ,

BIRTH_MONTH("Birth Month" , "birth_month" , "http://openid.net/schema/birthDate/birthMonth" , new ValueExtractor(){

			public String extractValue(User user) {
				
				if(user.getDob()!=null)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTime(user.getDob());
					int month = cal.get(Calendar.MONTH);
					return new Integer(month + 1).toString(); // returned month is zero based
				}
				else
					return "UNKNOWN";
		}}) ,

	BIRTH_YEAR("Birth Year" , "birth_year" , "http://openid.net/schema/birthDate/birthYear" , new ValueExtractor(){

			public String extractValue(User user) {
				
				if(user.getDob()!=null)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTime(user.getDob());
					int year = cal.get(Calendar.YEAR);
					return new Integer(year).toString(); 
				}
				else
					return "UNKNOWN";
		}}) ,
	
	BIRTH_DAY("Birth Day" , "birth_day" , "http://openid.net/schema/birthDate/birthday" , new ValueExtractor(){

			public String extractValue(User user) {
				
				if(user.getDob()!=null)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTime(user.getDob());
					int day = cal.get(Calendar.DAY_OF_MONTH);
					return new Integer(day).toString(); 
				}
				else
					return "UNKNOWN";
		}}) ,
		


	POSTCODE("Postal Code" , "postcode" , "http://openid.net/schema/contact/postalcode/business", new ValueExtractor(){

		public String extractValue(User user) {
			return user.getPostcode();
		}}) ;
	

	
	
	
	private  static Map<String,Attribute> sregLookupTable ;
	private  static Map<String,Attribute> axLookupTable ;
	private final static String CUSTOM_NS = "http://cci.emory.edu/schema/compatability/sreg";
	
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
		if(sregLookupTable == null) 
			sregLookupTable = new HashMap<String, Attribute>();
		sregLookupTable.put(sregName, attr);
	}
	
	private static void updateAxLookup(String axSchema , Attribute attr)
	{
		if(axLookupTable == null) 
			axLookupTable = new HashMap<String, Attribute>();
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
