package edu.emory.cci.bindaas.sts.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;

public interface ISecurityTokenServiceCore {

	@GET
	@Path("ping")
	public Response ping() ;
	
	@GET
	@Path("{identityServiceId}/issueToken")
	public Response issueToken(@PathParam("identityServiceId") String identityServiceId ,  @QueryParam("serviceProvider") String serviceProvider , @HeaderParam("Authorization") String encodedAuthorizationHeader);
	
	@POST
	@Path("{identityServiceId}/validateToken")
	public Response validateToken(@PathParam("identityServiceId") String identityServiceId , String token);
	
	
	@GET
	@Path("{identityServiceId}/extensions/getUsers")
	public Response getUsers(@PathParam("identityServiceId") String identityServiceId ) ;
	
	@GET
	@Path("{identityServiceId}/extensions/getGroups")
	public Response getGroups(@PathParam("identityServiceId") String identityServiceId ) throws IdentityProviderException;
	
	@POST
	@Path("{identityServiceId}/extensions/createOrModifyGroup")
	public Response createOrModifyGroup(@PathParam("identityServiceId") String identityServiceId , String body) throws IdentityProviderException;
	
	@POST
	@Path("{identityServiceId}/extensions/assignUserToGroups")
	public Response assignUserToGroups(@PathParam("identityServiceId") String identityServiceId , String body) throws IdentityProviderException;
	
	@POST
	@Path("{identityServiceId}/extensions/removeGroup")
	public Response removeGroup(@PathParam("identityServiceId") String identityServiceId , String body) throws IdentityProviderException;
	
	@POST
	@Path("{identityServiceId}/extensions/removeUsersFromGroup")
	public Response removeUsersFromGroup(@PathParam("identityServiceId") String identityServiceId , String body) throws IdentityProviderException;
	
	@POST
	@Path("{identityServiceId}/extensions/removeUsersFromAllGroups")
	public Response removeUsersFromAllGroups(@PathParam("identityServiceId") String identityServiceId , String body) throws IdentityProviderException;
	
}
