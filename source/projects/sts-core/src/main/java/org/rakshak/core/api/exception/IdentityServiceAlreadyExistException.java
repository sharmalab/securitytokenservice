package org.rakshak.core.api.exception;

public class IdentityServiceAlreadyExistException extends Exception {
	private static final long serialVersionUID = 1209467289050514166L;

	
	public IdentityServiceAlreadyExistException(String id) {
		super("Identity Service [" + id + "]");
	}
	

}
