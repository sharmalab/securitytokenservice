package org.rakshak.opensaml2.util;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

import org.rakshak.opensaml2.model.SAMLSigningKeyPair;

import sun.security.pkcs.PKCS8Key;
import sun.security.x509.X509Key;

public class SecurityUtil {

	/**
	 * Generate KeyPair that will be used to sign SAML documents
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static SAMLSigningKeyPair generateKeyPair() throws GeneralSecurityException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);
		
		KeyPair pair = keyGen.generateKeyPair();
		SAMLSigningKeyPair sskp = new SAMLSigningKeyPair();
		sskp.setPublicKeyDerBytes(X509Key.class.cast(pair.getPublic()).encode());
		sskp.setPrivateKeyDerBytes(PKCS8Key.class.cast(pair.getPrivate()).encode());
		return sskp;
	}
}
