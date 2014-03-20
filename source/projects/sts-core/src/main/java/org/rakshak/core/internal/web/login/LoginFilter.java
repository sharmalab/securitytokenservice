package org.rakshak.core.internal.web.login;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.rakshak.core.api.IIdentityService;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.exception.IdentityServiceNotFoundException;
import org.rakshak.core.api.model.Credential;
import org.rakshak.core.api.model.User;
import org.rakshak.core.bundle.ApplicationStarter;
import org.rakshak.core.internal.web.action.dashboard.AdminSession;
import org.rakshak.core.service.IManagerService;
import org.rakshak.core.util.VelocityEngineWrapper;

public class LoginFilter implements Filter  {

	private ApplicationStarter appStarter;
	private IManagerService managementService;
	private String bodyCenterTemplateName;
	private Template bodyCenterTemplate;
	private VelocityEngineWrapper velocityEngineWrapper;
	private String baseTemplateName;
	private Template baseTemplate;
	private String servletUrl;
	private String adminHomePage;
	
	
	private Log log = LogFactory.getLog(getClass());
	
	
	public String getAdminHomePage() {
		return adminHomePage;
	}

	public void setAdminHomePage(String adminHomePage) {
		this.adminHomePage = adminHomePage;
	}

	public String getServletUrl() {
		return servletUrl;
	}

	public void setServletUrl(String servletUrl) {
		this.servletUrl = servletUrl;
	}

	public IManagerService getManagementService() {
		return managementService;
	}

	public void setManagementService(IManagerService managementService) {
		this.managementService = managementService;
	}

	public String getBodyCenterTemplateName() {
		return bodyCenterTemplateName;
	}

	public void setBodyCenterTemplateName(String bodyCenterTemplateName) {
		this.bodyCenterTemplateName = bodyCenterTemplateName;
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

	public ApplicationStarter getAppStarter() {
		return appStarter;
	}

	public void setAppStarter(ApplicationStarter appStarter) {
		this.appStarter = appStarter;
	}

	public void destroy() {
		
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		AdminSession adminSession = AdminSession.getAdminSession(request.getSession());
		if(adminSession!= null)
		{
			if(request.getParameter("logout")!=null)
			{
				request.getSession().invalidate();
				response.sendRedirect(adminHomePage);
			}
			else
			{
				filterChain.doFilter(request, resp);
			}
			
		}
		else{
			
			if(request.getSession().isNew())
			{
				request.getSession().setAttribute("target", request.getRequestURL().toString());
			}
			
			String errorMessage = null;
			User user = null;
			if(request.getParameter("login")!=null)
			{
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				
				
				try{
					user = login(username, password);
					errorMessage = user!=null ? null : "Invalid Username or Password";
				}
				catch(Exception e)
				{
					log.error("Error authenticating user" , e);
					errorMessage = "Server Error";
				}
				
			}
			
			if(user!=null)
			{
				// create new session
				AdminSession.createAdminSession(request.getSession(), user);
				String targetUrl = request.getSession().getAttribute("target").toString();
				response.sendRedirect(targetUrl);
			}
			else{
				
				try{
				
				VelocityContext context = velocityEngineWrapper.createVelocityContext();
				context.put("userInformaation", getUserSection());
				context.put("pageTitle",  getPageTitleSection());
				context.put("pageSubTitle", getPageSubTitleSection());
				context.put("bodyLeftMargin", getBodyLeftMarginSection());
				context.put("bodyCenter",  getBodyCenterSection(errorMessage));
				context.put("bodyRightMargin", getBodyRightMarginSection());
				
				baseTemplate.merge(context, resp.getWriter());
				
				}catch(Exception e)
				{
					throw new ServletException(e);
				}
			}
			
		}
		
		
		
	}

	private String getBodyCenterSection(String errorMessage) throws IOException {
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("errorMessage", errorMessage);
		StringWriter sw = new StringWriter();
		bodyCenterTemplate.merge(context, sw);
		sw.close();
		return sw.toString();
	}

	private User login(String username, String password) throws IdentityProviderException, IdentityServiceNotFoundException
	{
		String adminServiceId = appStarter.getSystemConfiguration().getObject().getAdminIdentityService();
		IIdentityService identityService = managementService.getService(adminServiceId);
		boolean result =  identityService.authenticate(new Credential(username, password));
		if(result) return identityService.lookupUserByName(username); else return null;
	}
	public void init(FilterConfig arg0) throws ServletException {
		
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
		bodyCenterTemplate = velocityEngineWrapper.getVelocityTemplateByName(bodyCenterTemplateName);
		
	}

	public String getUserSection() throws Exception {
		
		return "";
	}

	public String getPageSubTitleSection() throws Exception {

		return "";
	}

	public String getPageTitleSection() throws Exception {
		
		return "";
	}

	public String getBodyRightMarginSection() throws Exception {

		return "";
	}

	public String getBodyLeftMarginSection() throws Exception {

		return "";
	}

	

}
