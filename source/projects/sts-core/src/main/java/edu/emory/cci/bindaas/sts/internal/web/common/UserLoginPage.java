package edu.emory.cci.bindaas.sts.internal.web.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.sts.util.VelocityEngineWrapper;

public class UserLoginPage {

	private String templateName;
	private VelocityEngineWrapper velocityEngineWrapper;
	private Template template;
	
	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	public void init() throws Exception
	{
		template = velocityEngineWrapper.getVelocityTemplateByName(templateName);
	}
	
	public void showLoginPage(HttpServletResponse response , String loginUrl , String cancelUrl , String sectionTitle , String pageTitle , String errorMessage , Map<String,String> hiddenFields ) throws Exception
	{
		
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("loginUrl" , loginUrl);
		context.put("cancelUrl" , cancelUrl);
		context.put("sectionTitle", sectionTitle);
		context.put("pageTitle", pageTitle);
		
		if(errorMessage!=null)
			context.put("errorMessage", errorMessage);
		context.put("hiddenFields", hiddenFields);		
		template.merge(context, response.getWriter());
		response.flushBuffer();
	}
	
	public void showLoginPage(HttpServletResponse response , String loginUrl , String cancelUrl  , String sectionTitle , String pageTitle , String errorMessage ) throws Exception
	{
		Map<String,String> hiddenFields = new HashMap<String, String>();
		showLoginPage(response, loginUrl , cancelUrl, sectionTitle , pageTitle , errorMessage , hiddenFields);
	}
	
	public void showLoginPage(HttpServletResponse response , String loginUrl , String cancelUrl  , String sectionTitle , String pageTitle ) throws Exception
	{
		Map<String,String> hiddenFields = new HashMap<String, String>();
		showLoginPage(response, loginUrl , cancelUrl, sectionTitle , pageTitle , null , hiddenFields);
	}
	
	public void showLoginPage(HttpServletResponse response , String loginUrl , String cancelUrl , String title ) throws Exception
	{
		Map<String,String> hiddenFields = new HashMap<String, String>();
		showLoginPage(response, loginUrl , cancelUrl, title , title , null , hiddenFields);
	}
	
	
	
	
}
