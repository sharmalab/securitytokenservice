package org.rakshak.core.internal.web.action.dashboard;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.rakshak.core.internal.web.action.dashboard.api.IBodyCenterSection;
import org.rakshak.core.internal.web.action.dashboard.api.IPageSubTitleSection;
import org.rakshak.core.internal.web.action.dashboard.api.IPageTitleSection;
import org.rakshak.core.util.VelocityEngineWrapper;

public class GettingStartedBodyCenterSection implements IBodyCenterSection , IPageTitleSection , IPageSubTitleSection {
	private VelocityEngineWrapper velocityEngineWrapper;
	private Template baseTemplate;
	private String baseTemplateName;
	private String createNewServiceUrl;
	
	public void init()
	{
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}
	
	public String getBodyCenterSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		StringWriter sw = new StringWriter();
		VelocityContext context = new VelocityContext();
		context.put("createNewServiceUrl", createNewServiceUrl);
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

	public String getCreateNewServiceUrl() {
		return createNewServiceUrl;
	}

	public void setCreateNewServiceUrl(String createNewServiceUrl) {
		this.createNewServiceUrl = createNewServiceUrl;
	}

	public String getPageSubTitleSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return "";
	}

	public String getPageTitleSection(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return "";
	}

}
