package edu.emory.cci.bindaas.sts.api.exception;

public class IdentityProviderException extends Exception {

	private static final long serialVersionUID = 3446207780119397935L;
	private String id;
	
	public IdentityProviderException(String id) {
		super();
		this.id = id;
	}

	public IdentityProviderException(String id , String arg0, Throwable arg1) {
		super(arg0, arg1);
		this.id = id;
	}

	public IdentityProviderException(String id , String arg0) {
		super(arg0);
		this.id = id;
	}

	public IdentityProviderException(String id , Throwable arg0) {
		super(arg0);
		this.id = id;
	}
	
	@Override
	public String getMessage()
	{
		return String.format("IdentityProvider=[%s]\tReason=[%s]" , id , super.getMessage());
	}

	
}
