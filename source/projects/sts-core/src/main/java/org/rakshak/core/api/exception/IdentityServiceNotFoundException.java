package org.rakshak.core.api.exception;


/**
 * Must be thrown when IIdentityService matching an 'id' is not found
 * @author nadir
 *
 */
public class IdentityServiceNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public IdentityServiceNotFoundException(String id) {
		super(String.format("No IIdentityService found for [%s]" , id ));
		
	}

}
