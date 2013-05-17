package edu.emory.cci.bindaas.sts.opensaml2.model;

import com.google.gson.annotations.Expose;

/**
 * Wrapper for KeyPair abstracting sun KeyPair class
 * @author nadir
 *
 */
public class SAMLSigningKeyPair {

	@Expose private byte[] privateKeyDerBytes;
	@Expose private byte[] publicKeyDerBytes;
	
	public byte[] getPrivateKeyDerBytes() { 
		return privateKeyDerBytes;
	}
	public void setPrivateKeyDerBytes(byte[] privateKeyDerBytes) {
		this.privateKeyDerBytes = privateKeyDerBytes;
	}
	public byte[] getPublicKeyDerBytes() {
		return publicKeyDerBytes;
	}
	public void setPublicKeyDerBytes(byte[] publicKeyDerBytes) {
		this.publicKeyDerBytes = publicKeyDerBytes;
	}
}
