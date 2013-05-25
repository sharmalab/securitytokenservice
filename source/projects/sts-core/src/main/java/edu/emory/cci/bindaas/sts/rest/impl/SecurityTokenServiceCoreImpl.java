package edu.emory.cci.bindaas.sts.rest.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.AuthenticationException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceNotFoundException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.SecureToken;
import edu.emory.cci.bindaas.sts.rest.api.ISecurityTokenServiceCore;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.StandardMimeType;

public class SecurityTokenServiceCoreImpl implements ISecurityTokenServiceCore {

	private IManagerService managerService;
	private Log log = LogFactory.getLog(getClass());
	private Map<String,Object> authFailedHeader ;
	
	public void init()
	{
		authFailedHeader = new HashMap<String, Object>();
		authFailedHeader.put("WWW-Authenticate", "Basic");
	}
	
	@GET
	@Path("ping")
	public Response ping() {
		return Response.ok().entity("I am alive").build();
	}

	@GET
	@Path("{identityServiceId}/issueToken")
	public Response issueToken(
			@PathParam("identityServiceId") String identityServiceId,
			@QueryParam("serviceProvider") String serviceProvider,
			@HeaderParam("Authorization") String encodedAuthorizationHeader) {
		
		try {
			
			Credential credential = getCredential(encodedAuthorizationHeader , identityServiceId);
			IIdentityService identityService = managerService.getService(identityServiceId);
			SecureToken token = identityService.issueToken(credential, serviceProvider);
			return RestUtil.createSuccessResponse(token.getContent(), StandardMimeType.XML.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (AuthenticationException e) {
			log.error(e);
			return RestUtil.createResponse("Invalid user credentials" , 401 , authFailedHeader );
		}
		catch (Exception e) {
			log.error("Error issuing token" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
		
	}

	public IManagerService getManagerService() {
		return managerService;
	}

	public void setManagerService(IManagerService managerService) {
		this.managerService = managerService;
	}

	@POST
	@Path("{identityServiceId}/validateToken")
	public Response validateToken(
			@PathParam("identityServiceId") String identityServiceId,
			String token) {
		try {
			IIdentityService identityService = managerService.getService(identityServiceId);
			boolean flag = identityService.validateToken(new SecureToken(token));
			JsonObject retVal = new JsonObject();
			retVal.add("authenticated", new JsonPrimitive(flag));
			retVal.add("authInstant", new JsonPrimitive(new Date().toString()));
			retVal.add("identityServiceId", new JsonPrimitive(identityServiceId));
			return RestUtil.createSuccessResponse(retVal.toString() , StandardMimeType.JSON.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (Exception e) {
			log.error("Error issuing token" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
	}
	
	public Credential getCredential(String encodedAuthorizationHeader , String id) throws AuthenticationException
	{
		try{
		encodedAuthorizationHeader = encodedAuthorizationHeader.replace("Basic", "").trim();
		String decodedValue = new String(Base64.decodeBase64(encodedAuthorizationHeader.getBytes()));
		String tokens[] = decodedValue.split(":");
		return new Credential(tokens[0], tokens[1]);
		}
		catch(Exception e)
		{
			throw new AuthenticationException(id);
		}
	}

}
