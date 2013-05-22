package edu.emory.cci.bindaas.sts.api.model;

public class SecureToken {
	private String content;

	public SecureToken(String content)
	{
		this.content = content;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
