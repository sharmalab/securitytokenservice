package edu.emory.cci.bindaas.sts.opensaml2.core;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;

import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerValue;
import sun.security.x509.X509Key;
import edu.emory.cci.bindaas.sts.opensaml2.model.SAMLSigningKeyPair;

public class TokenParams {

	public static Integer DEFAULT_TOKEN_LIFETIME = 3600; // 1 hr
	public static String DEFAULT_SUBJECT_QUALIFIER = "edu:emory:cci:bindaas:sts:subject:username"; // 
	public static String DEFAULT_SERVICE_PROVIDER = "http://example.org"; //
	
	public Credential getCredential() {
		return credential;
	}
	public void setCredential(Credential credential) {
		this.credential = credential;
	}
	public Map<String, List<String>> getCustomProperties() {
		return customProperties;
	}
	public void setCustomProperties(Map<String, List<String>> customProperties) {
		this.customProperties = customProperties;
	}
	public String getServiceProvider() {
		if(serviceProvider == null) return DEFAULT_SERVICE_PROVIDER;
		else
		return serviceProvider;
	}
	public void setServiceProvider(String serviceProvider) {
		this.serviceProvider = serviceProvider;
	}
	public Integer getLifetime() {
		if(lifetime == null) return DEFAULT_TOKEN_LIFETIME;
		else
		return lifetime;
	}
	public void setLifetime(Integer lifetime) {
		this.lifetime = lifetime;
	}
	public String getIssuer() {
		return issuer;
	}
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getSubjectQualifier() {
		if(subjectQualifier == null) return DEFAULT_SUBJECT_QUALIFIER;
		else
		return subjectQualifier;
	}
	public void setSubjectQualifier(String subjectQualifier) {
		this.subjectQualifier = subjectQualifier;
	}
	
	public void setCredential(SAMLSigningKeyPair signingKeyPair) throws IOException
	{
		PrivateKey privateKey = PKCS8Key.parse(new DerValue( signingKeyPair.getPrivateKeyDerBytes() ));
		PublicKey publicKey = X509Key.parse(new DerValue(signingKeyPair.getPublicKeyDerBytes()));
		BasicCredential basicCredential = new BasicCredential();
		basicCredential.setUsageType(UsageType.SIGNING);
		basicCredential.setPrivateKey(privateKey);
		basicCredential.setPublicKey(publicKey);
		this.credential = basicCredential;
	}
	
	public void setCredential(byte[] privateKeyDerBytes , byte[] publicKeyDerBytes) throws IOException
	{
		PrivateKey privateKey = PKCS8Key.parse(new DerValue( privateKeyDerBytes ));
		PublicKey publicKey = X509Key.parse(new DerValue(publicKeyDerBytes));
		BasicCredential basicCredential = new BasicCredential();
		basicCredential.setUsageType(UsageType.SIGNING);
		basicCredential.setPrivateKey(privateKey);
		basicCredential.setPublicKey(publicKey);
		this.credential = basicCredential;
	}
	
	private Credential credential;
	private Map<String,List<String>> customProperties;
	private String serviceProvider;
	private Integer lifetime;
	private String issuer;
	private String subject;
	private String subjectQualifier;

	
	
}
