package org.rakshak.core.internal.web.action.dashboard;

import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.rakshak.core.api.IIdentityService;
import org.rakshak.core.api.model.IdentityServiceRegistration;
import org.rakshak.core.bundle.ApplicationStarter;
import org.rakshak.core.internal.web.action.dashboard.api.IBodyCenterSection;
import org.rakshak.core.internal.web.action.dashboard.api.IPageSubTitleSection;
import org.rakshak.core.internal.web.action.dashboard.api.IPageTitleSection;
import org.rakshak.core.service.IManagerService;
import org.rakshak.core.util.VelocityEngineWrapper;

public class ViewServiceBodyCenterSection implements IBodyCenterSection, IPageTitleSection , IPageSubTitleSection{
	private VelocityEngineWrapper velocityEngineWrapper;
	private Template baseTemplate;
	private String baseTemplateName;
	private String gettingStartedUrl;
	private IManagerService managementService;
	private String createEditServletUrl;
	private ApplicationStarter appStarter;
	
	
	public void init()
	{
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}
	
	public String getBodyCenterSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String serviceId = request.getParameter("serviceId");
		IdentityServiceRegistration identityServiceReg;
		if(serviceId!=null)
		{
			IIdentityService identityService = managementService.getService(serviceId);
			identityServiceReg = identityService.getRegistrationInfo();
		}
		
		else
		{
			List<IdentityServiceRegistration> listOfIdentityServices = managementService.getAllService();
			if(listOfIdentityServices!=null && listOfIdentityServices.size() > 0)
			{
				identityServiceReg = listOfIdentityServices.get(0);
			}
			else
			{
				response.sendRedirect(gettingStartedUrl);
				return null;
			}
		}
		String issueTokenUrl = String.format("%s/securityTokenService/%s/issueToken", appStarter.getSystemConfiguration().getObject().getStsBaseUrl() , identityServiceReg.getId());
		String validateTokenUrl = String.format("%s/securityTokenService/%s/validateToken", appStarter.getSystemConfiguration().getObject().getStsBaseUrl() , identityServiceReg.getId());
		String openIdUrl = String.format("%s://%s:8080/rakshak/client/openid/%s",appStarter.getSystemConfiguration().getObject().getStsProtocol() , appStarter.getSystemConfiguration().getObject().getStsHost() , identityServiceReg.getId());
		
		StringWriter sw = new StringWriter();
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("identityServiceReg", identityServiceReg);
		context.put("createEditServletUrl", createEditServletUrl);
		context.put("issueTokenUrl", issueTokenUrl);
		context.put("validateTokenUrl", validateTokenUrl);
		context.put("openIdUrl", openIdUrl);
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



	public void setManagementService(IManagerService managementService) {
		this.managementService = managementService;
	}

	public String getGettingStartedUrl() {
		return gettingStartedUrl;
	}

	public void setGettingStartedUrl(String gettingStartedUrl) {
		this.gettingStartedUrl = gettingStartedUrl;
	}

	public String getCreateEditServletUrl() {
		return createEditServletUrl;
	}

	public void setCreateEditServletUrl(String createEditServletUrl) {
		this.createEditServletUrl = createEditServletUrl;
	}

	public String getPageSubTitleSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return "";
	}

	public String getPageTitleSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String serviceId = request.getParameter("serviceId");
		IdentityServiceRegistration identityServiceReg;
		if(serviceId!=null)
		{
			IIdentityService identityService = managementService.getService(serviceId);
			identityServiceReg = identityService.getRegistrationInfo();
		}
		
		else
		{
			List<IdentityServiceRegistration> listOfIdentityServices = managementService.getAllService();
			if(listOfIdentityServices!=null && listOfIdentityServices.size() > 0)
			{
				identityServiceReg = listOfIdentityServices.get(0);
			}
			else
			{
				return null;
			}
		}
		
		return identityServiceReg.getName();
	}

	public ApplicationStarter getAppStarter() {
		return appStarter;
	}

	public void setAppStarter(ApplicationStarter appStarter) {
		this.appStarter = appStarter;
	}


}
