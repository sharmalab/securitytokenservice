package edu.emory.cci.bindaas.sts.internal.web.common;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import edu.emory.cci.bindaas.sts.util.VelocityEngineWrapper;

public class ErrorPage {

	private String templateName;
	private VelocityEngineWrapper velocityEngineWrapper;
	private Template template;
	private Log log = LogFactory.getLog(getClass());
	
	private String baseTemplateName;
	private Template baseTemplate;

	public String getBaseTemplateName() {
		return baseTemplateName;
	}

	public void setBaseTemplateName(String baseTemplateName) {
		this.baseTemplateName = baseTemplateName;
	}

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

	public void init() throws Exception {
		template = velocityEngineWrapper
				.getVelocityTemplateByName(templateName);
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}

	private String getBodyCenterContent( HTTPError error,
			String detailedMessage, String suggestedAction) throws IOException 
	{
		StringWriter sw = new StringWriter();
		VelocityContext context = velocityEngineWrapper
				.createVelocityContext();
		context.put("error", error);
		context.put("detailedMessage", detailedMessage);
		context.put("suggestedAction", suggestedAction);
		template.merge(context, sw );
		sw.close();
		return sw.toString();
	}
	public void showErrorPage(HttpServletResponse response, HTTPError error,
			String detailedMessage, String suggestedAction) {
		try {
			response.setStatus(error.getStatusCode());
			String bodyCenterContent = getBodyCenterContent(error, detailedMessage, suggestedAction);
			
			VelocityContext context = velocityEngineWrapper.createVelocityContext();
			context.put("userInformaation", "");
			context.put("pageTitle",  "");
			context.put("pageSubTitle", "");
			context.put("bodyLeftMargin", "");
			context.put("bodyCenter",  bodyCenterContent);
			context.put("bodyRightMargin", "");
			baseTemplate.merge(context, response.getWriter());
			response.flushBuffer();
		} catch (Exception e) {
			log.fatal("Cannot generate error response", e);
			response.setStatus(HTTPError.INTERNAL_SERVER_ERROR.getStatusCode());
		}
	}

	public void showErrorPage(HttpServletResponse response, HTTPError error,
			String detailedMessage, String suggestedAction, Exception origin) {
		log.error(error.toString(), origin);
		showErrorPage(response, error, detailedMessage, suggestedAction);
	}

	public enum HTTPError {

		PAGE_NOT_FOUND(404 , "Page Not Found"), INTERNAL_SERVER_ERROR(500, "Internal Server Error"), NOT_AUTHORIZED(401 ,"You are not authorized to view this page");

		private HTTPError(Integer errorCode , String message) {
			this.statusCode = errorCode;
			this.message = message;
		}

		private Integer statusCode;
		private String message;
		
		public String getMessage() {
			return message;
		}

		

		public Integer getStatusCode() {
			return statusCode;
		}

	}

}
