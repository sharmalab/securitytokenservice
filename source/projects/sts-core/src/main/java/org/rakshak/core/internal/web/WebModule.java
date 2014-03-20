package org.rakshak.core.internal.web;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
/**
 * WebModule is an aggregation of GeneralServlets belonging to one module.
 *  
 * @author nadir
 *
 */
public  class WebModule {
	private String moduleName;
	private List<GeneralServlet> listOfServlets;
	private Log log = LogFactory.getLog(getClass());
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
			log.debug("Module [" + getModuleName() + "] registering Servlet [" + generalServlet.getClass().getSimpleName() + "] at [" + generalServlet.getServletUrl() + "]" );
			httpService.registerServlet(generalServlet.getServletUrl(),generalServlet, null, httpContext);
		}
		
	}
	
}
