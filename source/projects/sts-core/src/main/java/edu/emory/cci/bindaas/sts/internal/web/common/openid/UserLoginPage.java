package edu.emory.cci.bindaas.sts.internal.web.common.openid;

import java.io.IOException;
import java.io.StringWriter;
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
	
	private String baseTemplateName;
	private Template baseTemplate;
	
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

	
	
	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public String getBaseTemplateName() {
		return baseTemplateName;
	}

	public void setBaseTemplateName(String baseTemplateName) {
		this.baseTemplateName = baseTemplateName;
	}

	public void init() throws Exception
	{
		template = velocityEngineWrapper.getVelocityTemplateByName(templateName);
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}
	
	private String getBodySection( String loginUrl , String cancelUrl , String sectionTitle , String pageTitle , String errorMessage , Map<String,String> hiddenFields) throws IOException
	{
		StringWriter sw = new StringWriter();
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("loginUrl" , loginUrl);
		context.put("cancelUrl" , cancelUrl);
		context.put("sectionTitle", sectionTitle);
		context.put("pageTitle", pageTitle);
		
		if(errorMessage!=null)
			context.put("errorMessage", errorMessage);
		context.put("hiddenFields", hiddenFields);		
		template.merge(context, sw);
		
		sw.close();
		return sw.toString();
		
	}
	
	public void showLoginPage(HttpServletResponse response , String loginUrl , String cancelUrl , String sectionTitle , String pageTitle , String errorMessage , Map<String,String> hiddenFields ) throws Exception
	{
		
		String bodyContent = getBodySection(loginUrl, cancelUrl, sectionTitle, pageTitle, errorMessage, hiddenFields);
		
		VelocityContext context = new VelocityContext();
		context.put("userInformaation", "");
		context.put("pageTitle",  "");
		context.put("pageSubTitle", "");
		context.put("bodyLeftMargin", "");
		context.put("bodyCenter",  bodyContent);
		context.put("bodyRightMargin", "");
		baseTemplate.merge(context, response.getWriter());
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
