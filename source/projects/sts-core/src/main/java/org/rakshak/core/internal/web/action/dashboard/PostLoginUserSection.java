package org.rakshak.core.internal.web.action.dashboard;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.rakshak.core.internal.web.action.dashboard.api.IBodyRightMarginSection;
import org.rakshak.core.internal.web.action.dashboard.api.IUserSection;
import org.rakshak.core.util.VelocityEngineWrapper;

public  class PostLoginUserSection implements IUserSection , IBodyRightMarginSection{

	private VelocityEngineWrapper velocityEngineWrapper;
	private Template baseTemplate;
	private String baseTemplateName;
	
	public void init()
	{
		
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}
	
	public String getUserSection(HttpServletRequest request , HttpServletResponse response) throws Exception {
		StringWriter sw = new StringWriter();
		AdminSession adminSession = AdminSession.getAdminSession(request.getSession());
		String firstname = adminSession.getUser().getFirstName();
		if(firstname == null)
		{
			firstname = adminSession.getUser().getName();
		}
		
		VelocityContext context  = new VelocityContext();
		context.put("firstname", firstname);
		baseTemplate.merge(context, sw);
		sw.close();
		return sw.toString();
	}
	
//	public String getUserSection(HttpServletRequest request , HttpServletResponse response) throws Exception {
//		StringWriter sw = new StringWriter();
//		
//		String firstname = "Nadir Saghar";
//		VelocityContext context  = new VelocityContext();
//		context.put("firstname", firstname);
//		baseTemplate.merge(context, sw);
//		sw.close();
//		return sw.toString();
//	}
	
	
	public String getBodyRightMarginSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		return "";
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

	
	
}
