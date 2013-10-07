package edu.emory.cci.bindaas.sts.internal.web.common.openid;

import java.io.IOException;
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
import javax.servlet.http.HttpSession;

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
import edu.emory.cci.bindaas.sts.internal.web.common.UserLoginPage;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.GSONUtil;
import edu.emory.cci.bindaas.sts.util.VelocityEngineWrapper;

/**
 * registered at : /client/openid/*
 * pattern : /client/openid/{service}
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
			String[] tokenz = path.split(servletRegPath);
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
		    this.serverManagerCache.put(identityServiceId, serverManager);
		}
		
		log.debug("Serving OpenID request for [" + identityService.getRegistrationInfo().getName() + "]");
		
		processRequest(serverManager, identityService, req , resp);
		
		} catch (IdentityServiceNotFoundException e) {
			errorPage.showErrorPage(resp, HTTPError.PAGE_NOT_FOUND, "This page does not exist", null , e);
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
        	writeXrdsResponse(resp.getWriter() , serverManager.getOPEndpointUrl());
        	resp.setContentType("application/xrds+xml");
        	resp.flushBuffer();
        	return;
        }
	}
	
	
	private void writeXrdsResponse(Writer writer , String opEndpointUrl) throws IOException
	{
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("opEndpointUrl", opEndpointUrl);
		xrdsTemplate.merge(context, writer);
	}
	
	protected void processRequest(ServerManager serverManager , IIdentityService identityService ,HttpServletRequest req, HttpServletResponse resp ) throws Exception
	{
		try {
		
			Action action = null ; 
			try{
				
				action = Action.valueOf(req.getParameter("action").toString());
				log.debug("Serving Action [" + action + "]");
				switch(action)
				{
					case authenticate : handleUserAuthentication(serverManager , identityService , req , resp) ; break;
					case seekApproval : handleSeekApproval(serverManager , identityService , req , resp) ; break;
					case logout : handleLogout(serverManager , identityService , req , resp) ; break;
					case cancelled : handleUserCancellation(serverManager, req, resp) ; break;
					
				}
			}
			catch(Exception e){
				
				ParameterList parameterList = new ParameterList(req.getParameterMap());
				processRequest(serverManager, identityService,parameterList,req,resp);
			}
		
		}
		
		catch(Exception e)
		{
			throw new Exception("Request cannot be fulfilled", e);
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
		
		HttpSession session = req.getSession();
		if(session.getAttribute("context")!=null)
		{
			OpenIDSessionContext sessionContext = OpenIDSessionContext.class.cast(session.getAttribute("context"));
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
					
					Map<Attribute,Boolean> attributeRequested = OpenIdHelper.getAttributeRequested(serverManager, sessionContext.parameterList);
					Iterator<Entry<Attribute, Boolean>> iter = attributeRequested.entrySet().iterator();
					while(iter.hasNext())
					{
						Entry<Attribute, Boolean> entry = iter.next();
						if(entry.getValue() == false && optionalAttributes.contains(entry.getKey()) == false)
						{
							iter.remove() ; // remove optional attribute that wasn't authorized by the user
						}
					}
					
					sessionContext.attributeReleased = attributeRequested;
					sessionContext.setAuthorizedByUser(true);
					processRequest(serverManager, identityService, sessionContext.parameterList, req, resp);
				}
				{
					// user denied release of attributes. handle this .
					Message cancelled = serverManager.authResponse(sessionContext.parameterList, sessionContext.userSelectedId, sessionContext.userSelectedClaimedId, false);
					OpenIdHelper.sendIndirectResponse(resp, cancelled);
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
			throw new Exception("Illegal request. OpenIDSessionContext must be set in order to proceed");
		}
				
	}
	
	private void handleUserCancellation(ServerManager serverManager , HttpServletRequest req , HttpServletResponse resp) throws Exception
	{
		HttpSession session = req.getSession();
		if(session.getAttribute("context")!=null)
		{
			OpenIDSessionContext sessionContext = OpenIDSessionContext.class.cast(session.getAttribute("context"));
			Message cancelled = serverManager.authResponse(sessionContext.parameterList, sessionContext.userSelectedId, sessionContext.userSelectedClaimedId, false);
			OpenIdHelper.sendIndirectResponse(resp, cancelled);
		}
		else
		{
			throw new Exception("Illegal request. OpenIDSessionContext must be set in order to proceed");
		}
	}

	private void handleUserAuthentication(ServerManager serverManager,
			IIdentityService identityService, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		try {
			
		HttpSession session = req.getSession();
		if(session.getAttribute("context")!=null)
		{
			OpenIDSessionContext sessionContext = OpenIDSessionContext.class.cast(session.getAttribute("context"));
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			
			Credential credential = new Credential(username, password);
			log.debug("validating user credentials");
			if(identityService.authenticate(credential))
			{
				User user = identityService.lookupUserByName(username);
				String userSelectedId = serverManager.getOPEndpointUrl();
				String userSelectedClaimedId = String.format("%s?id=%s" , userSelectedId , user.getName());
				
				sessionContext.isLoggedIn = true;
				sessionContext.user = user;
				sessionContext.userSelectedClaimedId = userSelectedClaimedId;
				sessionContext.userSelectedId = userSelectedId;
				
				processRequest(serverManager, identityService,sessionContext.parameterList,req,resp);
				
			}
			else
			{
				// login again
				// display login page with error message
				showLoginPage(req, resp, "Invalid username or password" , identityService);
			}
			
		}
		else
		{
			throw new Exception("Illegal request. OpenIDSessionContext must be set in order to proceed");
		}
		
		}
		catch(AuthenticationException authenticationException)
		{
			// login again
			// display login page with error message
			showLoginPage(req, resp, "Invalid username or password" , identityService);
		}
	}

	
	
	private void processCheckIdImmediate(ServerManager serverManager,
			IIdentityService identityService, ParameterList parameterList,
			HttpServletRequest req, HttpServletResponse resp) throws Exception {
		HttpSession session  = req.getSession();
		OpenIDSessionContext sessionContext = session.getAttribute("context") !=null ? OpenIDSessionContext.class.cast(session.getAttribute("context")) : null;
		
		if(sessionContext.isLoggedIn() && sessionContext.isAuthorizedByUser())
		{
			// user logged in so respond with +ve response
			Message message = OpenIdHelper.buildAuthResponse(serverManager, sessionContext.user, sessionContext.parameterList, sessionContext.userSelectedId, sessionContext.userSelectedClaimedId, new HashSet<Attribute>());
			OpenIdHelper.sendIndirectResponse(resp, message);
		}
		{
			// respond with direct error message : setup_needed
			Message message = serverManager.authResponse(sessionContext.parameterList, sessionContext.userSelectedId, sessionContext.userSelectedClaimedId, false);
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
			HttpServletResponse resp , String optionalErrorMessage , IIdentityService identityService) throws Exception
	{
			String actionUrl = req.getRequestURL().toString()  + "?action=" + Action.authenticate;
			loginPage.showLoginPage(resp, actionUrl, String.format("%s OpenID Login Service", identityService.getRegistrationInfo().getName()), "OpendID Login", optionalErrorMessage);
	}
	
	
	private void processCheckIdSetup(ServerManager serverManager,
			IIdentityService identityService,ParameterList parameterList, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		
		HttpSession session  = req.getSession();
		OpenIDSessionContext sessionContext = session.getAttribute("context") !=null ? OpenIDSessionContext.class.cast(session.getAttribute("context")) : null;
		
		if(sessionContext == null)
		{
			log.debug("New session detected. Setting context information");
			sessionContext = new OpenIDSessionContext();
			session.setAttribute("context", sessionContext);
			session.setMaxInactiveInterval(sessionMaxInactiveInterval);
		}
		
		if(sessionContext.isLoggedIn())
		{
			// no need to login again
			log.debug("[" + sessionContext.user.getName() + "] already logged in. No need login again");
			if(sessionContext.isAuthorizedByUser && sessionContext.parameterList.equals(parameterList))
			{
				// authorized by user earlier and requesting same information as before
				log.debug("[" + sessionContext.user.getName() + "] already authorized response");
				Message message = OpenIdHelper.buildAuthResponse(serverManager, sessionContext.getUser(), parameterList, sessionContext.getUserSelectedId(), sessionContext.getUserSelectedClaimedId() , sessionContext.getAttributeReleased().keySet());
				OpenIdHelper.sendIndirectResponse(resp, message);
			}
			else if(sessionContext.parameterList.equals(parameterList) == false)
			{
				// authorized by user earlier but requesting different set of information 
				
				if(OpenIdHelper.isSubset(serverManager , parameterList, sessionContext.parameterList))
				{
					// request is seeking only a subset of attributes from last time - hence allow
					Message message = OpenIdHelper.buildAuthResponse(serverManager, sessionContext.getUser(), parameterList, sessionContext.getUserSelectedId(), sessionContext.getUserSelectedClaimedId() , sessionContext.getAttributeReleased().keySet());
					OpenIdHelper.sendIndirectResponse(resp, message);
				}
				else
				{
					// request is seeking more attributes than user authorized last time. User approval necessary
					log.debug("[" + sessionContext.user.getName() + "] need to re-authorize attribute release");
					sessionContext.parameterList = parameterList;
					Map<Attribute,Boolean> attributeRequested = OpenIdHelper.getAttributeRequested(serverManager, parameterList);
					showSeekApprovalPage( resp , sessionContext.user , sessionContext.parameterList.getParameterValue("openid.return_to") ,  attributeRequested);
				}
			}
			else
			{
				// never authorized by user before.
				Map<Attribute,Boolean> attributeRequested = OpenIdHelper.getAttributeRequested(serverManager, parameterList);
				if(attributeRequested.size() > 0)
				{
					// seek approval
					log.debug("[" + sessionContext.user.getName() + "] need to authorize attribute release");
					sessionContext.parameterList = parameterList;
					showSeekApprovalPage( resp , sessionContext.user , sessionContext.parameterList.getParameterValue("openid.return_to") ,  attributeRequested);
				}
				else
				{
					// no approval necessary as no attributes were requested
					log.debug("[" + sessionContext.user.getName() + "] : no attributes to release");
					sessionContext.parameterList = parameterList;
					sessionContext.setAuthorizedByUser(true);
					Message message = OpenIdHelper.buildAuthResponse(serverManager, sessionContext.getUser(), parameterList, sessionContext.getUserSelectedId(), sessionContext.getUserSelectedClaimedId() , sessionContext.getAttributeReleased().keySet());
					OpenIdHelper.sendIndirectResponse(resp, message);
				}
				
			}
			
		}
		else
		{
			// user not logged In . Show login page
			sessionContext.parameterList = parameterList;
			showLoginPage(req, resp, null , identityService);
		}
		
	}
	
	protected void showSeekApprovalPage(HttpServletResponse resp , User user , String relyingPartyUrl , Map<Attribute,Boolean> attributeRequested) throws Exception
	{

		URL relyingParty = new URL(relyingPartyUrl);
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("user", user);
		context.put("attributeRequested", attributeRequested);
		context.put("relyingParty", relyingParty.getHost());
		
		seekUserApprovalTemplate.merge(context, resp.getWriter());
		resp.flushBuffer();
		
	}

	public static enum Mode {associate, checkid_setup, checkid_immediate, check_authentication}
	
	public static enum Action { seekApproval, authenticate  , logout  , cancelled }
	
	public static class UserApprovalResponse
	{
		@Expose private boolean decision;
		@Expose private List<String> optionalAttributes;
	}
	
 	public static class OpenIDSessionContext {
		private boolean isLoggedIn;
		private boolean isAuthorizedByUser;
		private ParameterList parameterList;
		private String userSelectedId;
		private String userSelectedClaimedId;
		private User user;
		private Map<Attribute,Boolean> attributeReleased;
		
		
		public Map<Attribute, Boolean> getAttributeReleased() {
			return attributeReleased;
		}
		public void setAttributeReleased(Map<Attribute, Boolean> attributeReleased) {
			this.attributeReleased = attributeReleased;
		}
		public User getUser() {
			return user;
		}
		public void setUser(User user) {
			this.user = user;
		}
		public boolean isLoggedIn() {
			return isLoggedIn;
		}
		public void setLoggedIn(boolean isLoggedIn) {
			this.isLoggedIn = isLoggedIn;
		}
		public boolean isAuthorizedByUser() {
			return isAuthorizedByUser;
		}
		public void setAuthorizedByUser(boolean isAuthorizedByUser) {
			this.isAuthorizedByUser = isAuthorizedByUser;
		}
		public ParameterList getParameterList() {
			return parameterList;
		}
		public void setParameterList(ParameterList parameterList) {
			this.parameterList = parameterList;
		}
		public String getUserSelectedId() {
			return userSelectedId;
		}
		public void setUserSelectedId(String userSelectedId) {
			this.userSelectedId = userSelectedId;
		}
		public String getUserSelectedClaimedId() {
			return userSelectedClaimedId;
		}
		public void setUserSelectedClaimedId(String userSelectedClaimedId) {
			this.userSelectedClaimedId = userSelectedClaimedId;
		}
		
		
	}
	
}
