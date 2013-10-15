package edu.emory.cci.bindaas.sts.internal.web.common;

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
	}

	public void showErrorPage(HttpServletResponse response, HTTPError error,
			String detailedMessage, String suggestedAction) {
		try {
			response.setStatus(error.getStatusCode());
			VelocityContext context = velocityEngineWrapper
					.createVelocityContext();
			context.put("error", error);
			context.put("detailedMessage", detailedMessage);
			context.put("suggestedAction", suggestedAction);
			template.merge(context, response.getWriter());
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
