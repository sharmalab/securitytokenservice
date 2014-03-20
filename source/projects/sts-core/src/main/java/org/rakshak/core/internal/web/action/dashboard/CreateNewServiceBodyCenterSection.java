package org.rakshak.core.internal.web.action.dashboard;

import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.rakshak.core.api.IIdentityProvider;
import org.rakshak.core.internal.web.action.dashboard.api.IBodyCenterSection;
import org.rakshak.core.internal.web.action.dashboard.api.IPageSubTitleSection;
import org.rakshak.core.internal.web.action.dashboard.api.IPageTitleSection;
import org.rakshak.core.service.IManagerService;
import org.rakshak.core.util.VelocityEngineWrapper;

public class CreateNewServiceBodyCenterSection implements IBodyCenterSection, IPageTitleSection , IPageSubTitleSection{
	private VelocityEngineWrapper velocityEngineWrapper;
	private Template baseTemplate;
	private String baseTemplateName;
	private String createEditServletUrl;
	private String viewServiceServletUrl;

	private IManagerService managementService;
	
	public void init()
	{
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}
	
	public String getBodyCenterSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		List<IIdentityProvider> identityProviders = managementService.getIdentityProviders();
		StringWriter sw = new StringWriter();
		VelocityContext context = new VelocityContext();
		context.put("identityProviders", identityProviders);
		//context.put("createEditServletUrl", createEditServletUrl);
		context.put("viewServiceServletUrl", viewServiceServletUrl);
		baseTemplate.merge(context, sw);
		sw.close();
		return sw.toString();
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

	public IManagerService getManagementService() {
		return managementService;
	}


	public String getCreateEditServletUrl() {
		return createEditServletUrl;
	}

	public void setCreateEditServletUrl(String createEditServletUrl) {
		this.createEditServletUrl = createEditServletUrl;
	}

	public void setManagementService(IManagerService managementService) {
		this.managementService = managementService;
	}

	public String getViewServiceServletUrl() {
		return viewServiceServletUrl;
	}

	public void setViewServiceServletUrl(String viewServiceServletUrl) {
		this.viewServiceServletUrl = viewServiceServletUrl;
	}

	public String getPageSubTitleSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		return "";
	}

	public String getPageTitleSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		return "Create Identity Service";
	}


}
