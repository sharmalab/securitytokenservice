package edu.emory.cci.bindaas.sts.internal.web.common.openid;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.openid4java.message.AuthImmediateFailure;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.AuthenticationException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceNotFoundException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.internal.web.GeneralServlet;
import edu.emory.cci.bindaas.sts.internal.web.common.ErrorPage;
import edu.emory.cci.bindaas.sts.internal.web.common.ErrorPage.HTTPError;
import edu.emory.cci.bindaas.sts.internal.web.common.openid.OpenIDSession.RelyingPartyContext;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.GSONUtil;
import edu.emory.cci.bindaas.sts.util.VelocityEngineWrapper;

/**
 * registered at : /rakshak/client/openid/*
 * pattern : /rakshak/client/openid/{service}
 * @author nadir
 *
 */
public class OpenIDEndpointServlet extends GeneralServlet{

	private static final long serialVersionUID = -8624423972371500182L;
	private Map<String,ServerManager> serverManagerCache;
	private IManagerService managerService;
	private Integer sessionMaxInactiveInterval;
	private UserLoginPage loginPage;
	private Log log = LogFactory.getLog(getClass());
	private Template xrdsTemplate;
	private String xrdsTemplateName;
	private Template seekUserApprovalTemplate;
	private String seekUserApprovalTemplateName;
	private VelocityEngineWrapper velocityEngineWrapper;
	private ErrorPage errorPage;
	private String baseTemplateName;
	private Template baseTemplate;

	public String getBaseTemplateName() {
		return baseTemplateName;
	}

	public void setBaseTemplateName(String baseTemplateName) {
		this.baseTemplateName = baseTemplateName;
	}

	
	public String getSeekUserApprovalTemplateName() {
		return seekUserApprovalTemplateName;
	}

	public void setSeekUserApprovalTemplateName(String seekUserApprovalTemplateName) {
		this.seekUserApprovalTemplateName = seekUserApprovalTemplateName;
	}

	public String getXrdsTemplateName() {
		return xrdsTemplateName;
	}

	public void setXrdsTemplateName(String xrdsTemplateName) {
		this.xrdsTemplateName = xrdsTemplateName;
	}

	public ErrorPage getErrorPage() {
		return errorPage;
	}

	public void setErrorPage(ErrorPage errorPage) {
		this.errorPage = errorPage;
	}

	public Template getXrdsTemplate() {
		return xrdsTemplate;
	}

	public void setXrdsTemplate(Template xrdsTemplate) {
		this.xrdsTemplate = xrdsTemplate;
	}

	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}

	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}

	public UserLoginPage getLoginPage() {
		return loginPage;
	}

	public void setLoginPage(UserLoginPage loginPage) {
		this.loginPage = loginPage;
	}

	public IManagerService getManagerService() {
		return managerService;
	}

	public void setManagerService(IManagerService managerService) {
		this.managerService = managerService;
	}

	public Integer getSessionMaxInactiveInterval() {
		return sessionMaxInactiveInterval;
	}

	public void setSessionMaxInactiveInterval(Integer sessionMaxInactiveInterval) {
		this.sessionMaxInactiveInterval = sessionMaxInactiveInterval;
	}

	public void init()
	{
		serverManagerCache = new HashMap<String, ServerManager>();
		xrdsTemplate = velocityEngineWrapper.getVelocityTemplateByName(xrdsTemplateName);
		seekUserApprovalTemplate = velocityEngineWrapper.getVelocityTemplateByName(seekUserApprovalTemplateName);
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}
	
	private String extractIdentityServiceIdFromRequest(HttpServletRequest req)
	{
	
	try {
			URI uri = new URI(req.getRequestURL().toString());
			String path = uri.getPath();
			String servletRegPath = getServletUrl();
			String[] tokenz = path.split(servletRegPath + "/");
			String trailingSuffix = tokenz[1];
			String pathSegments[] = trailingSuffix.split("/");
			return pathSegments[0];
			
			
		}catch(Exception e)
		{
			log.error("Unable to extract IdentityServiceID from request " , e);
			return null;
		}
		
	}
	
	private void processRequest(HttpServletRequest req , HttpServletResponse resp)
	{
		// extract identity service name
		String identityServiceId = extractIdentityServiceIdFromRequest(req);  
	try {
		
		IIdentityService identityService = 	managerService.getService(identityServiceId);
		ServerManager serverManager = null;
		if(serverManagerCache.containsKey(identityServiceId))
		{
			serverManager = serverManagerCache.get(identityServiceId);
		}
		else
		{
			serverManager = new ServerManager();
			serverManager.setOPEndpointUrl(req.getRequestURL().toString());
			serverManager.setPrivateAssociations(new InMemoryServerAssociationStore());
		    serverManager.setSharedAssociations(new InMemoryServerAssociationStore());
		    serverManager.setEnforceRpId(false); // TODO : change it to true and solve xerces class not found issue.
		    this.serverManagerCache.put(identityServiceId, serverManager);
		}
		
		log.debug("Serving OpenID request for [" + identityService.getRegistrationInfo().getName() + "]");
		
		processRequest(serverManager, identityService, req , resp);
		
		} catch (IdentityServiceNotFoundException e) {
			errorPage.showErrorPage(resp, HTTPError.PAGE_NOT_FOUND, "Page does not exist", "Please check the URL and try again" , e);
		}
		catch (Exception e)
		{
			errorPage.showErrorPage(resp, HTTPError.INTERNAL_SERVER_ERROR, e.getMessage() , "Please contact server administrator" , e);
		}
	}
	
	
	protected void processRequest(ServerManager serverManager , IIdentityService identityService , ParameterList parameterList , HttpServletRequest req, HttpServletResponse resp ) throws Exception
	{
		String m = parameterList.hasParameter("openid.mode") ?
				parameterList.getParameterValue("openid.mode") : null;
        if(m!=null){
        	try {
		        	Mode mode = Mode.valueOf(m);
		        	log.debug("Processing openid.mode [" + mode + "]");
		        	switch(mode)
		    		{
		    			case associate : processAssociationRequest(serverManager,parameterList, req,resp);break;
		    			case check_authentication : processAuthenticationRequest(serverManager,parameterList ,req,resp);break;
		    			case checkid_setup : processCheckIdSetup(serverManager, identityService ,parameterList, req , resp) ; break;
		    			case checkid_immediate : processCheckIdImmediate(serverManager, identityService , parameterList, req , resp) ; break;
		    			
		    		}
        	}catch(IllegalArgumentException e)
        	{
        		throw new Exception("Invalid openid mode specified = [" + m + "]");
        	}
        	
        }else{
        	// assume discovery request
        	log.debug("Sending discovery response");
        	
        	if(req.getParameter("id")!=null)
        		writeXrdsResponse(resp.getWriter() , serverManager.getOPEndpointUrl() , true);
        	else
        		writeXrdsResponse(resp.getWriter() , serverManager.getOPEndpointUrl() , false);
        	resp.setContentType("application/xrds+xml");
        	resp.flushBuffer();
        	return;
        }
	}
	
	private void writeXrdsResponse(Writer writer , String opEndpointUrl, boolean isLocalDiscovery) throws IOException
	{
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("opEndpointUrl", opEndpointUrl);
		context.put("localDiscovery", isLocalDiscovery);
		xrdsTemplate.merge(context, writer);
	}
	
	protected void processRequest(ServerManager serverManager , IIdentityService identityService ,HttpServletRequest req, HttpServletResponse resp ) throws Exception
	{
		Action action = null ; 
		try{
			String act = req.getParameter("action")!=null ? req.getParameter("action").toString() : "";
			action = Action.valueOf(act);
			log.debug("Serving Action [" + action + "]");
			
			switch(action)
			{
				case authenticate : handleUserAuthentication(serverManager , identityService , req , resp) ; break;
				case seekApproval : handleSeekApproval(serverManager , identityService , req , resp) ; break;
				case logout : handleLogout(serverManager , identityService , req , resp) ; break;
				case cancelled : handleUserCancellation(serverManager,identityService, req, resp) ; break;
				
			}
		}
		catch(IllegalArgumentException e){
			
			ParameterList parameterList = new ParameterList(req.getParameterMap());
			OpenIdHelper.logRequestParameters(parameterList);
			processRequest(serverManager, identityService,parameterList,req,resp);
		}
	}
	
	

	private void handleLogout(ServerManager serverManager,
			IIdentityService identityService, HttpServletRequest req,
			HttpServletResponse resp) {
		req.getSession().invalidate();
	}

	private void handleSeekApproval(ServerManager serverManager,
			IIdentityService identityService, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		
		OpenIDSession openidSession = OpenIDSession.getOpenIDSessionContext(req , identityService.getRegistrationInfo().getId());
		String relyingParty = req.getParameter("relyingParty");
		RelyingPartyContext relyingPartyContext;
		if(openidSession !=null && relyingParty !=null && (relyingPartyContext = openidSession.lookupRelyingParty(relyingParty))!=null )
		{
			
			String response = req.getParameter("response");
			if(response!=null)
			{
				UserApprovalResponse userApprovalResponse = GSONUtil.getGSONInstance().fromJson(response, UserApprovalResponse.class);
				if(userApprovalResponse.decision == true)
				{
					// user allowed release of attributes
					Set<Attribute> optionalAttributes = new HashSet<Attribute>();
					if(userApprovalResponse.optionalAttributes !=null)
					{
						for(String optAttr : userApprovalResponse.optionalAttributes)
						{
							try{
								Attribute attr = Attribute.valueOf(optAttr);
								optionalAttributes.add(attr);
							}catch(IllegalArgumentException e){
								// invalid attribute
							}
						}
					}
					
					Map<Attribute,Boolean> attributeRequested = OpenIdHelper.getAttributeRequested(serverManager, relyingPartyContext.getParameterList());
					Iterator<Entry<Attribute, Boolean>> iter = attributeRequested.entrySet().iterator();
					while(iter.hasNext())
					{
						Entry<Attribute, Boolean> entry = iter.next();
						if(entry.getValue() == false && optionalAttributes.contains(entry.getKey()) == false)
						{
							iter.remove() ; // remove optional attribute that wasn't authorized by the user
						}
					}
					
					relyingPartyContext.setAttributeReleased(attributeRequested);
					relyingPartyContext.setAuthorizedByUser(true);
					processRequest(serverManager, identityService, relyingPartyContext.getParameterList(), req, resp);
				}
				{
					// user denied release of attributes. handle this .
					Message cancelled = serverManager.authResponse(relyingPartyContext.getParameterList(), openidSession.getUserSelectedId(), openidSession.getUserSelectedClaimedId(), false);
					OpenIdHelper.sendIndirectResponse(resp, cancelled);
					return;
				}
			}
			else
			{
				throw new Exception("Missing response in payload");
			}

		}
		else
		{
			// invalid state . no valid session found
			throw new Exception("Illegal request. OpenIDSession must be set in order to proceed");
		}
				
	}
	
	private void handleUserCancellation(ServerManager serverManager ,IIdentityService identityService , HttpServletRequest req , HttpServletResponse resp) throws Exception
	{
		OpenIDSession openidSession = OpenIDSession.getOpenIDSessionContext(req , identityService.getRegistrationInfo().getId());
		String relyingParty = req.getParameter("relyingParty");
		RelyingPartyContext relyingPartyContext;
		if(openidSession !=null && relyingParty !=null && (relyingPartyContext = openidSession.lookupRelyingParty(relyingParty))!=null )
		{
			Message cancelled = serverManager.authResponse(relyingPartyContext.getParameterList(), openidSession.getUserSelectedId() , openidSession.getUserSelectedClaimedId() , false);
			OpenIdHelper.sendIndirectResponse(resp, cancelled);
			return;
		}
		else
		{
			throw new Exception("Illegal request. OpenIDSession must be set in order to proceed");
		}
	}

	private void handleUserAuthentication(ServerManager serverManager,
			IIdentityService identityService, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		

			OpenIDSession openidSession = OpenIDSession
					.getOpenIDSessionContext(req, identityService.getRegistrationInfo().getId());
			String relyingParty = req.getParameter("relyingParty");
			RelyingPartyContext relyingPartyContext;
			if (openidSession != null
					&& relyingParty != null
					&& (relyingPartyContext = openidSession
							.lookupRelyingParty(relyingParty)) != null) {
				
				String username = req.getParameter("username");
				String password = req.getParameter("password");

				Credential credential = new Credential(username, password);
				log.debug("validating user credentials");
				
				try {
				if (identityService.authenticate(credential)) {
					User user = identityService.lookupUserByName(username);
				
					String userSelectedClaimedId = serverManager
							.getOPEndpointUrl() + "?id=" + user.getName();
					String userSelectedId = userSelectedClaimedId;

					openidSession.setLoggedIn(true);
					openidSession.setUser(user);
					openidSession.setUserSelectedId(userSelectedId);
					openidSession.setUserSelectedClaimedId(userSelectedClaimedId);
					openidSession.setOpEndpointUrl(serverManager
							.getOPEndpointUrl());
					

					processRequest(serverManager, identityService,
							relyingPartyContext.getParameterList(), req, resp);

				} else {
					// login again
					// display login page with error message
					showLoginPage(req, resp, "Invalid username or password",
							identityService , relyingPartyContext.getRelyingPartyDomain() );
				}
				}catch(AuthenticationException e)
				{
					log.error(e);
					showLoginPage(req, resp, "Invalid username or password",
							identityService , relyingPartyContext.getRelyingPartyDomain() );
				}

			} else {
				throw new Exception(
						"Illegal request. OpenIDSession must be set in order to proceed");
			}

	}	
	
	private void processCheckIdImmediate(ServerManager serverManager,
			IIdentityService identityService, ParameterList parameterList,
			HttpServletRequest req, HttpServletResponse resp) throws Exception {
		
		OpenIDSession openidSession = OpenIDSession
				.getOpenIDSessionContext(req , identityService.getRegistrationInfo().getId());
		
		RelyingPartyContext relyingPartyContext;
		if (openidSession != null
				&& (relyingPartyContext = openidSession
						.lookupRelyingParty(parameterList)) != null && openidSession.isLoggedIn() && relyingPartyContext.isAuthorizedByUser())
		{
			// user logged in so respond with +ve response
			Message message = OpenIdHelper.buildAuthResponse(serverManager, openidSession.getUser(),relyingPartyContext.getParameterList(),openidSession.getUserSelectedId(), openidSession.getUserSelectedClaimedId(), new HashSet<Attribute>());
			OpenIdHelper.sendIndirectResponse(resp, message);
			return;
		}
		{
			// respond with direct error message : setup_needed
			Message message = serverManager.authResponse(parameterList, null , null , false); // TODO : test this scenario
			if(message instanceof AuthImmediateFailure)
			{
				OpenIdHelper.sendDirectResponse(resp, message);
			}
			else
			{
				throw new Exception("Expecting OpenID4Java to return instanceof AuthImmediateFailure. Found [" + message.getClass().getName() + "]");
			}
			
		}
		
	}
	

	private void processAssociationRequest(ServerManager serverManager,ParameterList parameterList,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		Message message = OpenIdHelper.processAssociationRequest(serverManager, parameterList);
		OpenIdHelper.sendDirectResponse(resp, message);
		log.debug("Sent Association Response");
	}

	private void processAuthenticationRequest(ServerManager serverManager,ParameterList parameterList,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Message message = serverManager.verify(parameterList);
		OpenIdHelper.sendDirectResponse(resp, message);
		log.debug("Sent Authentication Response");
	}

	private void showLoginPage(HttpServletRequest req,
			HttpServletResponse resp , String optionalErrorMessage , IIdentityService identityService , String relyingParty) throws Exception
	{
			String actionUrl = req.getRequestURL().toString() ;
			String loginUrl = actionUrl + "?action=" + Action.authenticate.toString();
			String cancelUrl = actionUrl + "?action=" + Action.cancelled.toString();
			
			Map<String,String> hiddenField = new HashMap<String, String>();
			hiddenField.put("relyingParty", relyingParty);
			
			loginPage.showLoginPage(resp, loginUrl,cancelUrl, identityService.getRegistrationInfo().getName(), identityService.getRegistrationInfo().getName() , optionalErrorMessage , hiddenField);
	}
	
	
	private void processCheckIdSetup(ServerManager serverManager,
			IIdentityService identityService,ParameterList parameterList, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		
		OpenIDSession openidSession = OpenIDSession.getOpenIDSessionContext(req, identityService.getRegistrationInfo().getId());
		
		if(openidSession == null)
		{
			log.debug("New session detected. Setting context information");
			openidSession = OpenIDSession.createOpenIDSessionContext( req, identityService.getRegistrationInfo().getId() , this.sessionMaxInactiveInterval);
		}
		
		RelyingPartyContext relyingPartyContext = openidSession.lookupRelyingParty(parameterList);
		
		if(relyingPartyContext == null)
		{
			relyingPartyContext = openidSession.addRelyingParty(parameterList);
			log.debug("Adding new relying party [" + relyingPartyContext + "]");
		}
		
		if(openidSession.isLoggedIn())
		{
			// no need to login again
			log.debug("[" + openidSession.getUser().getName() + "] already logged in. No need login again");
			if(relyingPartyContext!=null &&   relyingPartyContext.isAuthorizedByUser() && relyingPartyContext.getParameterList().equals(parameterList))
			{
				// authorized by user earlier and requesting same information as before
				log.debug("[" + openidSession.getUser().getName() + "] already authorized response");
				Message message = OpenIdHelper.buildAuthResponse(serverManager, openidSession.getUser(), parameterList, openidSession.getUserSelectedId(), openidSession.getUserSelectedClaimedId() , relyingPartyContext.getAttributeReleased().keySet());
				OpenIdHelper.sendIndirectResponse(resp, message);
				return;
			}
			else if(relyingPartyContext!=null &&   relyingPartyContext.isAuthorizedByUser() && relyingPartyContext.getParameterList().equals(parameterList) == false)
			{
				// authorized by user earlier but requesting different set of information 
				
				if(OpenIdHelper.isSubset(serverManager , parameterList, relyingPartyContext.getParameterList()))
				{
					// request is seeking only a subset of attributes from last time - hence allow
					Message message = OpenIdHelper.buildAuthResponse(serverManager, openidSession.getUser(), parameterList, openidSession.getUserSelectedId(), openidSession.getUserSelectedClaimedId() ,relyingPartyContext.getAttributeReleased().keySet());
					OpenIdHelper.sendIndirectResponse(resp, message);
					return;
				}
				else
				{
					// request is seeking more attributes than user authorized last time. User approval necessary
					log.debug("[" + openidSession.getUser().getName() + "] need to re-authorize attribute release");
					relyingPartyContext.setParameterList(parameterList);
					Map<Attribute,Boolean> attributeRequested = OpenIdHelper.getAttributeRequested(serverManager, parameterList);
					showSeekApprovalPage( resp , openidSession.getUser() , parameterList.getParameterValue("openid.return_to") ,  attributeRequested);
				}
			}
			else
			{
				// never authorized by user before.
		
				Map<Attribute,Boolean> attributeRequested = OpenIdHelper.getAttributeRequested(serverManager, parameterList);
				if(attributeRequested.size() > 0)
				{
					// seek approval
					log.debug("[" + openidSession.getUser().getName() + "] need to authorize attribute release");
					relyingPartyContext.setParameterList(parameterList);
					showSeekApprovalPage( resp , openidSession.getUser() , parameterList.getParameterValue("openid.return_to") ,  attributeRequested);
				}
				else
				{
					// no approval necessary as no attributes were requested
					log.debug("[" + openidSession.getUser().getName() + "] : no attributes to release");
					relyingPartyContext.setParameterList(parameterList);
					relyingPartyContext.setAttributeReleased(attributeRequested);
					relyingPartyContext.setAuthorizedByUser(true);
					Message message = OpenIdHelper.buildAuthResponse(serverManager, openidSession.getUser(), parameterList, openidSession.getUserSelectedId(), openidSession.getUserSelectedClaimedId() , relyingPartyContext.getAttributeReleased().keySet());
					OpenIdHelper.sendIndirectResponse(resp, message);
					return;
				}
				
			}
			
		}
		else
		{
			// user not logged In . Show login page
			relyingPartyContext.setParameterList(parameterList);
			showLoginPage(req, resp, null , identityService , relyingPartyContext.getRelyingPartyDomain());
		}
		
	}
	
	private void showSeekApprovalPage(HttpServletResponse response , User user , String relyingPartyUrl , Map<Attribute,Boolean> attributeRequested) throws Exception
	{
		String bodyCenterSection = getBodyCenterSection(user, relyingPartyUrl, attributeRequested);
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("userInformaation", "");
		context.put("pageTitle",  "");
		context.put("pageSubTitle", "");
		context.put("bodyLeftMargin", "");
		context.put("bodyCenter",  bodyCenterSection);
		context.put("bodyRightMargin", "");
		baseTemplate.merge(context, response.getWriter());
		response.flushBuffer();
		
	}
	
	private String getBodyCenterSection(User user , String relyingPartyUrl , Map<Attribute,Boolean> attributeRequested) throws IOException
	{
		StringWriter sw = new StringWriter();
		URL relyingParty = new URL(relyingPartyUrl);
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("user", user);
		context.put("attributeRequested", attributeRequested);
		context.put("relyingParty", relyingParty.getHost());
		
		seekUserApprovalTemplate.merge(context, sw );
		sw.close();
		return sw.toString();
	}

	public static enum Mode {associate, checkid_setup, checkid_immediate, check_authentication}
	
	public static enum Action { seekApproval, authenticate  , logout  , cancelled }
	
	public static class UserApprovalResponse
	{
		@Expose private boolean decision;
		@Expose private List<String> optionalAttributes;
	}
	
}
