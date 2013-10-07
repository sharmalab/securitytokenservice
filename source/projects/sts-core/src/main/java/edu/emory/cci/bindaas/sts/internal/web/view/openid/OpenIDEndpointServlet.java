package edu.emory.cci.bindaas.sts.internal.web.view.openid;

import java.io.IOException;
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

import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;

import com.google.gson.annotations.Expose;

import edu.emory.cci.bindaas.sts.api.IIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.AuthenticationException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityServiceNotFoundException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.User;
import edu.emory.cci.bindaas.sts.internal.web.GeneralServlet;
import edu.emory.cci.bindaas.sts.service.IManagerService;
import edu.emory.cci.bindaas.sts.util.GSONUtil;

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
	
	private void processRequest(HttpServletRequest req , HttpServletResponse resp)
	{
		String identityServiceId = null; // extract identity service name 
		try {
		
		ServerManager serverManager = null;
		if(serverManagerCache.containsKey(identityServiceId))
		{
			serverManager = serverManagerCache.get(identityServiceId);
		}
		else
		{
			serverManager = new ServerManager();
			serverManager.setOPEndpointUrl(req.getServletPath());
			serverManager.setPrivateAssociations(new InMemoryServerAssociationStore());
		    serverManager.setSharedAssociations(new InMemoryServerAssociationStore());
		    this.serverManagerCache.put(identityServiceId, serverManager);
		}
		
		IIdentityService identityService = 	managerService.getService(identityServiceId);
		processRequest(serverManager, identityService, req , resp);
		
		} catch (IdentityProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); // throw 500 internal server error
		} catch (IdentityServiceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); // throw 404 page not found
		}
	}
	
	
	protected void processRequest(ServerManager serverManager , IIdentityService identityService , ParameterList parameterList , HttpServletRequest req, HttpServletResponse resp ) throws Exception
	{
		String m = parameterList.hasParameter("openid.mode") ?
				parameterList.getParameterValue("openid.mode") : null;
        if(m!=null){
        	try {
		        	Mode mode = Mode.valueOf(m);
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
        	String xrdsResponse = OpenIdHelper.createXrdsResponse(serverManager.getOPEndpointUrl());
        	resp.setContentType("application/xrds+xml");
        	resp.getWriter().append(xrdsResponse);
        	resp.getWriter().close();
        	return;
        }
	}
	protected void processRequest(ServerManager serverManager , IIdentityService identityService ,HttpServletRequest req, HttpServletResponse resp ) throws IdentityProviderException
	{
		try {
		
			Action action = null ; 
			try{
				action = Action.valueOf(req.getParameter("action").toString());
			}
			catch(Exception e){
				// either null or invalid action specified
			}
			
			if(action!=null)
			{
				switch(action)
				{
					case authenticate : handleUserAuthentication(serverManager , identityService , req , resp) ; break;
					case seekApproval : handleSeekApproval(serverManager , identityService , req , resp) ; break;
					case logout : handleLogout(serverManager , identityService , req , resp) ; break;
					case cancelled : handleUserCancellation(serverManager, req, resp) ; break;
					
				}
			}
			else
			{
				ParameterList parameterList = new ParameterList(req.getParameterMap());
				processRequest(serverManager, identityService,parameterList,req,resp);				
			}
		
		}
		
		catch(Exception e)
		{
			throw new IdentityProviderException("Request cannot be fulfilled", e);
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
				}
				{
					// user denied release of attributes. handle this . TODO:
					Message cancelled = serverManager.authResponse(sessionContext.parameterList, sessionContext.userSelectedId, sessionContext.userSelectedClaimedId, false);
					resp.sendRedirect(cancelled.getDestinationUrl(true));
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
			resp.sendRedirect(cancelled.getDestinationUrl(true));
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
				showLoginPage(req, resp, "Invalid username or password");
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
			showLoginPage(req, resp, "Invalid username or password");
		}
	}

	
	
	private void processCheckIdImmediate(ServerManager serverManager,
			IIdentityService identityService, ParameterList parameterList,
			HttpServletRequest req, HttpServletResponse resp) {
		// TODO Auto-generated method stub
		
	}

	private void processAssociationRequest(ServerManager serverManager,ParameterList parameterList,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		Message message = OpenIdHelper.processAssociationRequest(serverManager, parameterList);
		OpenIdHelper.sendDirectResponse(resp, message);
	}

	private void processAuthenticationRequest(ServerManager serverManager,ParameterList parameterList,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {

		Message message = serverManager.verify(parameterList);
		OpenIdHelper.sendDirectResponse(resp, message);
		
	}

	private void showLoginPage(HttpServletRequest req,
			HttpServletResponse resp , String optionalErrorMessage)
	{
		// TODO
	}
	
	
	private void processCheckIdSetup(ServerManager serverManager,
			IIdentityService identityService,ParameterList parameterList, HttpServletRequest req,
			HttpServletResponse resp) throws IOException, MessageException {
		
		HttpSession session  = req.getSession();
		OpenIDSessionContext sessionContext = session.getAttribute("context") !=null ? OpenIDSessionContext.class.cast(session.getAttribute("context")) : null;
		
		if(sessionContext == null)
		{
			sessionContext = new OpenIDSessionContext();
			session.setAttribute("context", sessionContext);
			session.setMaxInactiveInterval(sessionMaxInactiveInterval);
		}
		
		if(sessionContext.isLoggedIn())
		{
			// no need to login again
			if(sessionContext.isAuthorizedByUser && sessionContext.parameterList.equals(parameterList))
			{
				// authorized by user earlier and requesting same information as before
				
				Message message = OpenIdHelper.buildAuthResponse(serverManager, sessionContext.getUser(), parameterList, sessionContext.getUserSelectedId(), sessionContext.getUserSelectedClaimedId() , sessionContext.getAttributeReleased().keySet());
				OpenIdHelper.sendDirectResponse(resp, message);
			}
			else if(sessionContext.parameterList.equals(parameterList) == false)
			{
				// authorized by user earlier but requesting different set of information 
				if(OpenIdHelper.isSubset(serverManager , parameterList, sessionContext.parameterList))
				{
					// request is seeking only a subset of attributes from last time - hence allow
					Message message = OpenIdHelper.buildAuthResponse(serverManager, sessionContext.getUser(), parameterList, sessionContext.getUserSelectedId(), sessionContext.getUserSelectedClaimedId() , sessionContext.getAttributeReleased().keySet());
					OpenIdHelper.sendDirectResponse(resp, message);
				}
				else
				{
					// request is seeking more attributes than user authorized last time. User approval necessary
					sessionContext.parameterList = parameterList;
					Map<Attribute,Boolean> attributeRequested = OpenIdHelper.getAttributeRequested(serverManager, parameterList);
					showSeekApprovalPage(req, resp , attributeRequested);
				}
			}
			else
			{
				// never authorized by user before.
				Map<Attribute,Boolean> attributeRequested = OpenIdHelper.getAttributeRequested(serverManager, parameterList);
				if(attributeRequested.size() > 0)
				{
					// seek approval
					sessionContext.parameterList = parameterList;
					showSeekApprovalPage(req, resp , attributeRequested);
				}
				else
				{
					// no approval necessary as no attributes were requested
					sessionContext.parameterList = parameterList;
					sessionContext.setAuthorizedByUser(true);
					Message message = OpenIdHelper.buildAuthResponse(serverManager, sessionContext.getUser(), parameterList, sessionContext.getUserSelectedId(), sessionContext.getUserSelectedClaimedId() , sessionContext.getAttributeReleased().keySet());
					OpenIdHelper.sendDirectResponse(resp, message);
				}
				
			}
			
		}
		else
		{
			// user not logged In . Show login page
			sessionContext.parameterList = parameterList;
			showLoginPage(req, resp, null);
		}
		
	}
	
	protected void showSeekApprovalPage(HttpServletRequest req, HttpServletResponse resp , Map<Attribute,Boolean> attributeRequested)
	{
		// TODO : show seek approval page
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
