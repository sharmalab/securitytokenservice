package org.rakshak.opensaml2.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.saml2.core.impl.AssertionUnmarshaller;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xml.security.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.rakshak.opensaml2.exception.TokenGeneratorException;
import org.rakshak.opensaml2.model.SAMLSigningKeyPair;
import org.rakshak.opensaml2.util.SecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerValue;
import sun.security.x509.X509Key;

public class TokenGenerator {
	private static XMLObjectBuilderFactory builderFactory;
	private static BasicParserPool ppMgr = new BasicParserPool();
	private static Log log = LogFactory.getLog(TokenGenerator.class);
	private static boolean INITIALIZED = false;
	public static void init() throws ConfigurationException
	{
		if(!INITIALIZED)
		{
			ppMgr.setNamespaceAware(true);
			// OpenSAML 2.3
			DefaultBootstrap.bootstrap();
			INITIALIZED = true;
		}
	}
	public static XMLObjectBuilderFactory getSAMLBuilder()
			throws ConfigurationException {

		if (builderFactory == null) {
			init();
			builderFactory = Configuration.getBuilderFactory();
		}

		return builderFactory;
	}

	public static Issuer createIssuer(String issuer)
			throws ConfigurationException {
		Issuer iss = createType(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		iss.setValue(issuer);
		iss.setFormat(Issuer.ENTITY);
		return iss;
	}

	public static Assertion createAssertion(TokenParams tokenParams,
			DateTime notBefore, DateTime notAfter)
			throws ConfigurationException {
		Assertion assertion = createType(Assertion.class,
				Assertion.DEFAULT_ELEMENT_NAME);
		assertion.setIssuer(createIssuer(tokenParams.getIssuer()));
		assertion.setIssueInstant(notBefore);
		assertion.setConditions(createConditions(
				tokenParams.getServiceProvider(), notBefore, notAfter));
		assertion.setSubject(createSubject(tokenParams.getSubject(),
				tokenParams.getSubjectQualifier(), notBefore, notAfter));
		
		return assertion;
	}

	
	
	public static String generateToken(TokenParams tokenParams)
			throws TokenGeneratorException {
		// Create the assertion
		try {
			DateTime notBefore = new DateTime();
			DateTime notAfter = notBefore
					.plusSeconds(tokenParams.getLifetime());
			// create Assertion
			Assertion assertion = createAssertion(tokenParams, notBefore,
					notAfter);

			// create custom attributes
			if(tokenParams.getCustomProperties()!=null)
			{
				AttributeStatement attributeStatement = createAttributeStatement(tokenParams.getCustomProperties());
				assertion.getAttributeStatements().add(attributeStatement);
			}
			
			// create Signature
			Signature signature = (Signature) getSAMLBuilder().getBuilder(
					Signature.DEFAULT_ELEMENT_NAME).buildObject(
					Signature.DEFAULT_ELEMENT_NAME);

			signature.setSigningCredential(tokenParams.getCredential());
			signature
					.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
			signature
					.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
			signature.setKeyInfo(getKeyInfo(tokenParams.getCredential()));

			assertion.setSignature(signature);

			AssertionMarshaller marshaller = new AssertionMarshaller();
			Element plaintextElement = marshaller.marshall(assertion);
			Signer.signObject(signature);
			plaintextElement = marshaller.marshall(assertion);
			String assertionString = XMLHelper.nodeToString(plaintextElement);
			return assertionString;
		} catch (Exception e) {
			throw new TokenGeneratorException(e);
		}

	}

	public static AttributeStatement createAttributeStatement(Map<String,List<String>> customAttributes) throws ConfigurationException
	{
		AttributeStatement attributeStatement = createType(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);
		for(String key : customAttributes.keySet())
		{
			Attribute attr = buildStringAttribute(key, customAttributes.get(key));
			attributeStatement.getAttributes().add(attr);
		}
		return attributeStatement;
	}
	public static KeyInfo getKeyInfo(Credential credential)
			throws SecurityException {
		SecurityConfiguration secConfiguration = Configuration
				.getGlobalSecurityConfiguration();
		NamedKeyInfoGeneratorManager namedKeyInfoGeneratorManager = secConfiguration
				.getKeyInfoGeneratorManager();
		KeyInfoGeneratorManager keyInfoGeneratorManager = namedKeyInfoGeneratorManager
				.getDefaultManager();
		KeyInfoGeneratorFactory factory = keyInfoGeneratorManager
				.getFactory(credential);
		KeyInfoGenerator generator = factory.newInstance();
		return generator.generate(credential);

	}

	public static NameID createNameID(String username, String nameQualifier)
			throws ConfigurationException {
		NameID nameID = createType(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
		nameID.setValue(username);
		nameID.setNameQualifier(nameQualifier);
		nameID.setFormat(NameID.ENTITY);
		return nameID;
	}

	public static SubjectConfirmation createSubjectConfirmation(
			DateTime notBefore, DateTime notAfter)
			throws ConfigurationException {

		SubjectConfirmationData confirmationData = createType(
				SubjectConfirmationData.class,
				SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
		confirmationData.setNotBefore(notBefore);
		confirmationData.setNotOnOrAfter(notAfter);

		SubjectConfirmation subjectConfirmation = createType(
				SubjectConfirmation.class,
				SubjectConfirmation.DEFAULT_ELEMENT_NAME);
		subjectConfirmation.setSubjectConfirmationData(confirmationData);
		subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
		return subjectConfirmation;
	}

	public static Subject createSubject(String username, String nameQualifier,
			DateTime notBefore, DateTime notAfter)
			throws ConfigurationException {
		NameID nameID = createNameID(username, nameQualifier);
		SubjectConfirmation subjectConfirmation = createSubjectConfirmation(
				notBefore, notAfter);

		Subject subject = createType(Subject.class,
				Subject.DEFAULT_ELEMENT_NAME);

		subject.setNameID(nameID);
		subject.getSubjectConfirmations().add(subjectConfirmation);

		return subject;
	}

	public static <T> T createType(Class<T> element, QName type)
			throws ConfigurationException {
		@SuppressWarnings("rawtypes")
		SAMLObjectBuilder builder = (SAMLObjectBuilder) getSAMLBuilder()
				.getBuilder(type);
		return element.cast(builder.buildObject());
	}

	public static Conditions createConditions(String audience,
			DateTime notBefore, DateTime notAfter)
			throws ConfigurationException {

		Conditions conditions = createType(Conditions.class,
				Conditions.DEFAULT_ELEMENT_NAME);
		conditions.setNotBefore(notBefore);
		conditions.setNotOnOrAfter(notAfter);

		AudienceRestriction audienceRestriction = createType(
				AudienceRestriction.class,
				AudienceRestriction.DEFAULT_ELEMENT_NAME);

		Audience aud = createType(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
		aud.setAudienceURI(audience);

		audienceRestriction.getAudiences().add(aud);
		conditions.getAudienceRestrictions().add(audienceRestriction);

		return conditions;
	}

	public static Attribute buildStringAttribute(String name, String value)
			throws ConfigurationException {

		Attribute attrName = createType(Attribute.class,
				Attribute.DEFAULT_ELEMENT_NAME);
		attrName.setName(name);

		// Set custom Attributes
		@SuppressWarnings({ "unchecked", "unused" })
		XMLObjectBuilder<XSString> stringBuilder = getSAMLBuilder().getBuilder(
				XSString.TYPE_NAME);
		XSString attrValueName = stringBuilder.buildObject(XSString.TYPE_NAME);
		attrValueName.setValue(value);

		attrName.getAttributeValues().add(attrValueName);
		return attrName;
	}

	public static Attribute buildStringAttribute(String name, List<String> values)
			throws ConfigurationException {

		Attribute attrName = createType(Attribute.class,
				Attribute.DEFAULT_ELEMENT_NAME);
		attrName.setName(name);

		// Set custom Attributes
		for(String value : values)
		{
			@SuppressWarnings({ "unchecked", "unused" })
			XMLObjectBuilder<XSString> stringBuilder = getSAMLBuilder().getBuilder(
					XSString.TYPE_NAME);
			XSString attrValueName = stringBuilder.buildObject(XSString.TYPE_NAME);
			attrValueName.setValue(value);

			attrName.getAttributeValues().add(attrValueName);

		}
		return attrName;
	}

	public static boolean validateSignature(String token,
			SAMLSigningKeyPair signingKeyPair) throws TokenGeneratorException {
		try {
			PrivateKey privateKey = PKCS8Key.parse(new DerValue(signingKeyPair
					.getPrivateKeyDerBytes()));
			PublicKey publicKey = X509Key.parse(new DerValue(signingKeyPair
					.getPublicKeyDerBytes()));
			BasicCredential basicCredential = new BasicCredential();
			basicCredential.setUsageType(UsageType.SIGNING);
			basicCredential.setPrivateKey(privateKey);
			basicCredential.setPublicKey(publicKey);
			return validateSignature(token, basicCredential);
		} catch (TokenGeneratorException tokenE) {
			throw tokenE;
		} catch (Exception e) {
			log.error("Unable to perform Signature Validation", e);
			throw new TokenGeneratorException(e);
		}
	}
	public static boolean validateSignature(String token, Credential credential)
			throws TokenGeneratorException {
		try {
			init();
			InputStream in = new ByteArrayInputStream(token.getBytes());
			Document inCommonMDDoc = ppMgr.parse(in);
			AssertionUnmarshaller unmarshaller = new AssertionUnmarshaller();
			Assertion assertion = (Assertion) unmarshaller
					.unmarshall(inCommonMDDoc.getDocumentElement());
			SignatureValidator validator = new SignatureValidator(credential);
			try {
				validator.validate(assertion.getSignature());
				return true;
			} catch (ValidationException e) {
				log.error("Invalid Signature", e);
				return false;
			}
		} catch (Exception e) {
			log.error("Unable to perform Signature Validation", e);
			throw new TokenGeneratorException(e);
		}
	}
// TODO : remove this code and add to test cases
	public static void main(String[] args) throws TokenGeneratorException,
			Exception {
		TokenParams param = new TokenParams();
		param.setIssuer("caGrid Dorian 1.3 Production Grid");
		param.setLifetime(5000);
		param.setServiceProvider("http://ClearCanvas.org");
		param.setSubject("nadir");
		Map<String,List<String>> pt = new HashMap<String, List<String>>();
		pt.put("SomeAttr" , Arrays.asList(new String[] { "SomeVal1" , "SomeVal2"}));
		param.setCustomProperties(pt);
		
		SAMLSigningKeyPair signingKeyPair = SecurityUtil.generateKeyPair();
		param.setCredential(signingKeyPair);
		
		String token = generateToken(param);
		
		System.out.println(token);
		
		System.out.println("Validating " + validateSignature(token, signingKeyPair));
	}

}
