package edu.emory.cci.bindaas.sts.api.exception;

import edu.emory.cci.bindaas.sts.api.model.ServiceOperation;

/**
 * Must be thrown when IdentityProvider does not support a particular ServiceOperation
 * @author nadir
 *
 */
public class MethodNotImplementedException extends IdentityProviderException {
	private static final long serialVersionUID = 1L;

	public MethodNotImplementedException(String id , ServiceOperation serviceOperation) {
		super(id , String.format("IdentityProvider does not support method [%s]" , serviceOperation.toString()));
		
	}

}
