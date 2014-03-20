package org.rakshak.core.internal.web.action.dashboard;

import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.rakshak.core.api.model.IdentityServiceRegistration;
import org.rakshak.core.internal.web.action.dashboard.api.IBodyLeftMarginSection;
import org.rakshak.core.service.IManagerService;
import org.rakshak.core.util.VelocityEngineWrapper;

public class ServiceMenuLeftMarginSection implements IBodyLeftMarginSection {
	private VelocityEngineWrapper velocityEngineWrapper;
	private Template baseTemplate;
	private String baseTemplateName;
	private String createNewServiceUrl;
	private String viewServiceUrl;
	private IManagerService managementService;
	
	public void init()
	{
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}
	
	

	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	public String getBaseTemplateName() {
		return baseTemplateName;
	}

	public void setBaseTemplateName(String baseTemplateName) {
		this.baseTemplateName = baseTemplateName;
	}

	public String getCreateNewServiceUrl() {
		return createNewServiceUrl;
	}

	public void setCreateNewServiceUrl(String createNewServiceUrl) {
		this.createNewServiceUrl = createNewServiceUrl;
	}

	public IManagerService getManagementService() {
		return managementService;
	}



	public void setManagementService(IManagerService managementService) {
		this.managementService = managementService;
	}



	public String getViewServiceUrl() {
		return viewServiceUrl;
	}



	public void setViewServiceUrl(String viewServiceUrl) {
		this.viewServiceUrl = viewServiceUrl;
	}



	public String getBodyLeftMarginSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		List<IdentityServiceRegistration> listOfIdentityServices = managementService.getAllService();
		
		StringWriter sw = new StringWriter();
		VelocityContext context = new VelocityContext();
		context.put("createNewServiceUrl", createNewServiceUrl);
		context.put("viewServiceUrl", viewServiceUrl);
		context.put("listOfIdentityServices", listOfIdentityServices);
		context.put("showMenu" , listOfIdentityServices.size() > 0);
		baseTemplate.merge(context, sw);
		sw.close();
		return sw.toString();
	}

}
