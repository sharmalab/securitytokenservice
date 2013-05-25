package edu.emory.cci.bindaas.sts.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

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
	
}
