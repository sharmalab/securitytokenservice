package org.rakshak.core.internal.web.action.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rakshak.core.api.exception.IdentityProviderException;
import org.rakshak.core.api.exception.IdentityServiceAlreadyExistException;
import org.rakshak.core.api.model.IdentityServiceRegistration;
import org.rakshak.core.service.IManagerService;
import org.rakshak.core.util.GSONUtil;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

/**
 * Servlet bound to /rakshak/administration/create_edit_identity_service.action
 * @author nadir
 *
 */
public class CreateEditIdentityServiceServlet extends GeneralBaseTemplateServlet{
	private static final long serialVersionUID = 3918931456151909296L;
	
	private IManagerService managerService;
	private Log log = LogFactory.getLog(getClass());
	private String viewServiceUrl;
	

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String response = req.getParameter("response");
		
		if(response!=null)
		{
			Request request = GSONUtil.getGSONInstance().fromJson(response, Request.class);
			try {
				
				IdentityServiceRegistration serviceReg = managerService.registerService(request.identityProviderId, request.name, request.description, request.configuration , true);
				resp.getWriter().write(viewServiceUrl  + "?serviceId=" + serviceReg.getId() );
			} catch (IdentityProviderException e) {
				log.error(e);
				throw new ServletException(e);
			} catch (IdentityServiceAlreadyExistException e) {
				// do nothing
			}
		}
		else
			throw new ServletException("[response] field is empty");
	}
	
	
	public IManagerService getManagerService() {
		return managerService;
	}


	public void setManagerService(IManagerService managerService) {
		this.managerService = managerService;
	}


	public String getViewServiceUrl() {
		return viewServiceUrl;
	}


	public void setViewServiceUrl(String viewServiceUrl) {
		this.viewServiceUrl = viewServiceUrl;
	}


	public static class Request {
		@Expose private String identityProviderId;
		@Expose private String name;
		@Expose private String description;
		@Expose private JsonObject configuration;
	}
	
	

}
