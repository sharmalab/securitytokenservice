package edu.emory.cci.bindaas.sts.api.exception;

public class AuthorizationException extends IdentityProviderException{
	private static final long serialVersionUID = 1L;

	public AuthorizationException(String id) {
		super(id);

	}

}
