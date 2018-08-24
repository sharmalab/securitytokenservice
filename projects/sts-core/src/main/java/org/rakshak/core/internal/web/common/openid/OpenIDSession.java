package org.rakshak.core.internal.web.common.openid;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openid4java.message.ParameterList;
import org.rakshak.core.api.model.User;

public class OpenIDSession {
	public static final String ATTR_NAME = "openid-context";
	private boolean isLoggedIn;
	private User user;
	private String opEndpointUrl;
	private String userSelectedId;
	private String userSelectedClaimedId;
	private Map<String,RelyingPartyContext> relyingParties;
	
	
	public String getOpEndpointUrl() {
		return opEndpointUrl;
	}
	public void setOpEndpointUrl(String opEndpointUrl) {
		this.opEndpointUrl = opEndpointUrl;
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
	public Map<String, RelyingPartyContext> getRelyingParties() {
		return relyingParties;
	}
	public void setRelyingParties(Map<String, RelyingPartyContext> relyingParties) {
		this.relyingParties = relyingParties;
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
	
	public static OpenIDSession getOpenIDSessionContext(HttpServletRequest request , String identityService)
	{
		HttpSession session = request.getSession();
		String key = String.format("%s:%s", ATTR_NAME ,  identityService);
		OpenIDSession context = session.getAttribute(key) !=null ? OpenIDSession.class.cast(session.getAttribute(key))  : null ;
		return context;
	}
	
	
	public static OpenIDSession createOpenIDSessionContext(HttpServletRequest request , String identityService , Integer maxInactivePeriod)
	{
		HttpSession session = request.getSession();
		String key = String.format("%s:%s", ATTR_NAME ,  identityService);
		OpenIDSession context = new OpenIDSession();
		context.setRelyingParties(new HashMap<String, OpenIDSession.RelyingPartyContext>());
		session.setAttribute(key, context);
		session.setMaxInactiveInterval(maxInactivePeriod);
		return context;
	}
	
	public RelyingPartyContext addRelyingParty(ParameterList parameterList) throws MalformedURLException
	{
		RelyingPartyContext relyingParty = new RelyingPartyContext(parameterList);
		this.relyingParties.put(relyingParty.relyingPartyDomain, relyingParty);
		return relyingParty;
	}
	
	public RelyingPartyContext lookupRelyingParty(ParameterList parameterList) throws MalformedURLException
	{
		String rpDomain = RelyingPartyContext.extractRPDomain(parameterList);
		return this.relyingParties.get(rpDomain);
	}
	
	public RelyingPartyContext lookupRelyingParty(String rpDomain) throws MalformedURLException
	{
		return this.relyingParties.get(rpDomain);
	}
	
	public static class RelyingPartyContext {
		private boolean isAuthorizedByUser;
		private ParameterList parameterList;
		private Map<Attribute,Boolean> attributeReleased;
		
		private String relyingPartyDomain; // used for identifying RP
		
		
		public String toString(){
			return relyingPartyDomain;
		}
		
		public boolean equals(Object that)
		{
			return (that!=null && that instanceof RelyingPartyContext && RelyingPartyContext.class.cast(that).relyingPartyDomain.equals(this.relyingPartyDomain));
		}
		
		public int hashCode()
		{
			return relyingPartyDomain.hashCode();
		}
		
		public static String extractRPDomain(ParameterList parameterList) throws MalformedURLException
		{
			String return_to = parameterList.getParameterValue("openid.return_to");
			URL url = new URL(return_to);
			return url.getHost();
		}
		
		public RelyingPartyContext(ParameterList parameterList) throws MalformedURLException
		{
			this.parameterList = parameterList;
			this.relyingPartyDomain = RelyingPartyContext.extractRPDomain(parameterList);
		}
		
		public String getRelyingPartyDomain() {
			return relyingPartyDomain;
		}
		public void setRelyingPartyDomain(String relyingPartyDomain) {
			this.relyingPartyDomain = relyingPartyDomain;
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
		
		public Map<Attribute, Boolean> getAttributeReleased() {
			return attributeReleased;
		}
		public void setAttributeReleased(Map<Attribute, Boolean> attributeReleased) {
			this.attributeReleased = attributeReleased;
		}
	}
}
