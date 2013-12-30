package edu.emory.cci.bindaas.sts.rest.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.AuthenticationException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceNotFoundException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.api.model.SecureToken;
import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.rest.api.ISecurityTokenServiceCore;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.GSONUtil;
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

	@GET
	@Path("{identityServiceId}/extensions/getUsers")
	public Response getUsers(@PathParam("identityServiceId") String identityServiceId ) 
	{
		try {
			IIdentityService identityService = managerService.getService(identityServiceId);
			Collection<User> users = identityService.getUsers();
			
			String jsonized = GSONUtil.getGSONInstance().toJson(users);
					
			return RestUtil.createSuccessResponse( jsonized , StandardMimeType.JSON.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (Exception e) {
			log.error("Error executing operation" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
		
	}
	
	@GET
	@Path("{identityServiceId}/extensions/getGroups")
	public Response getGroups(@PathParam("identityServiceId") String identityServiceId ) throws IdentityProviderException
	{
		try {
			IIdentityService identityService = managerService.getService(identityServiceId);
			Collection<Group> groups = identityService.getGroups();
			
			String jsonized = GSONUtil.getGSONInstance().toJson(groups);
					
			return RestUtil.createSuccessResponse( jsonized , StandardMimeType.JSON.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (Exception e) {
			log.error("Error executing operation" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
	}
	
	@POST
	@Path("{identityServiceId}/extensions/createOrModifyGroup")
	public Response createOrModifyGroup(@PathParam("identityServiceId") String identityServiceId , String body) {
		try {
			IIdentityService identityService = managerService.getService(identityServiceId);
			CreateOrModifyGroupPayload createOfModifyGroupPayload = GSONUtil.getGSONInstance().fromJson(body, CreateOrModifyGroupPayload.class);
			
			Group group = identityService.createOrModifyGroup(createOfModifyGroupPayload.groupName, createOfModifyGroupPayload.groupDescription, createOfModifyGroupPayload.users, createOfModifyGroupPayload.createGroupIfNotExist);
			
			String jsonized = GSONUtil.getGSONInstance().toJson(group);
					
			return RestUtil.createSuccessResponse( jsonized , StandardMimeType.JSON.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (Exception e) {
			log.error("Error executing operation" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
	}
	
	@POST
	@Path("{identityServiceId}/extensions/assignUserToGroups")
	public Response assignUserToGroups(@PathParam("identityServiceId") String identityServiceId , String body) {
		try {
			IIdentityService identityService = managerService.getService(identityServiceId);
			AssignUserToGroupsPayload assignUserToGroupsPayload = GSONUtil.getGSONInstance().fromJson(body, AssignUserToGroupsPayload.class);
			
			User user = identityService.assignUserToGroups(assignUserToGroupsPayload.username, assignUserToGroupsPayload.groups);
			
			String jsonized = GSONUtil.getGSONInstance().toJson(user);
					
			return RestUtil.createSuccessResponse( jsonized , StandardMimeType.JSON.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (Exception e) {
			log.error("Error executing operation" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
	}
	
	
	@POST
	@Path("{identityServiceId}/extensions/removeGroup")
	public Response removeGroup(@PathParam("identityServiceId") String identityServiceId , String body) {
		try {
			IIdentityService identityService = managerService.getService(identityServiceId);
			RemoveGroupPayload removeGroupPayload = GSONUtil.getGSONInstance().fromJson(body, RemoveGroupPayload.class);
			
			Group group = identityService.removeGroup(removeGroupPayload.group);
			
			String jsonized = GSONUtil.getGSONInstance().toJson(group);
					
			return RestUtil.createSuccessResponse( jsonized , StandardMimeType.JSON.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (Exception e) {
			log.error("Error executing operation" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
	}
	
	@POST
	@Path("{identityServiceId}/extensions/removeUsersFromGroup")
	public Response removeUsersFromGroup(@PathParam("identityServiceId") String identityServiceId , String body) {
		try {
			IIdentityService identityService = managerService.getService(identityServiceId);
			RemoveUsersFromGroupPayload removeUsersFromGroupPayload = GSONUtil.getGSONInstance().fromJson(body, RemoveUsersFromGroupPayload.class);
			
			Group group = identityService.removeUsersFromGroup(removeUsersFromGroupPayload.group, removeUsersFromGroupPayload.users);
			
			String jsonized = GSONUtil.getGSONInstance().toJson(group);
					
			return RestUtil.createSuccessResponse( jsonized , StandardMimeType.JSON.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (Exception e) {
			log.error("Error executing operation" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
	}
	
	@POST
	@Path("{identityServiceId}/extensions/removeUsersFromAllGroups")
	public Response removeUsersFromAllGroups(@PathParam("identityServiceId") String identityServiceId , String body) {
		try {
			IIdentityService identityService = managerService.getService(identityServiceId);
			RemoveUsersFromAllGroupsPayload removeUsersFromAllGroupsPayload = GSONUtil.getGSONInstance().fromJson(body, RemoveUsersFromAllGroupsPayload.class);
			
			identityService.removeUsersFromAllGroups(removeUsersFromAllGroupsPayload.users);
			
			String jsonized = "{ \"result\": \"success\"}";
					
			return RestUtil.createSuccessResponse( jsonized , StandardMimeType.JSON.toString());
		} 
		catch(IdentityServiceNotFoundException e)
		{
			log.error(e);
			return RestUtil.createResponse(e.getMessage() , 404 );
		}
		catch (Exception e) {
			log.error("Error executing operation" , e);
			return RestUtil.createErrorResponse(e.getMessage());
		}
	}
	
	
	public static class CreateOrModifyGroupPayload{
		@Expose private String groupName;
		@Expose private String groupDescription;
		@Expose private Set<String> users;
		@Expose private boolean createGroupIfNotExist;
		
	}
	
	public static class AssignUserToGroupsPayload{
		@Expose private String username;
		@Expose private Set<String> groups;
	}
	
	public static class RemoveUsersFromGroupPayload {
		@Expose private String group;
		@Expose private Set<String> users;
		
	}
	
	public static class RemoveGroupPayload {
		@Expose private String group;
	}
	
	public static class RemoveUsersFromAllGroupsPayload{
		@Expose private Set<String> users;
	}
	
}
