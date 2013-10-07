package edu.emory.cci.bindaas.sts.internal.web;

import javax.servlet.http.HttpServlet;

public  class GeneralServlet extends HttpServlet{
	private static final long serialVersionUID = 3041070499860594750L;
	private String servletUrl;
	
	public void setServletUrl(String servletUrl) {
		this.servletUrl = servletUrl;
	}

	public String getServletUrl() {
		return servletUrl;
	}


}
