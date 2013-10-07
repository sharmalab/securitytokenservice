package edu.emory.cci.bindaas.sts.internal.web;

import java.util.List;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

public  class WebAppInitializer {
	private String moduleName;
	private List<GeneralServlet> listOfServlets;
	
	public List<GeneralServlet> getListOfServlets() {
		return listOfServlets;
	}

	public void setListOfServlets(List<GeneralServlet> listOfServlets) {
		this.listOfServlets = listOfServlets;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public  void init(HttpService httpService , HttpContext httpContext) throws Exception
	{
		for(GeneralServlet generalServlet : this.listOfServlets)
		{
			httpService.registerServlet(generalServlet.getServletUrl(),generalServlet, null, httpContext);
		}
		
	}
	
}
