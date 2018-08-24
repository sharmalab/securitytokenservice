package org.rakshak.core.internal.web.action.dashboard;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.rakshak.core.api.model.User;

public class AdminSession {
	private User user;
	private Date loggedInTime;
	private String pageLastVisited;
	
	public final static String ATTR_NAME = "admin-session";
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Date getLoggedInTime() {
		return loggedInTime;
	}
	public void setLoggedInTime(Date loggedInTime) {
		this.loggedInTime = loggedInTime;
	}
	public String getPageLastVisited() {
		return pageLastVisited;
	}
	public void setPageLastVisited(String pageLastVisited) {
		this.pageLastVisited = pageLastVisited;
	}
	
	
	public static AdminSession getAdminSession(HttpSession httpSession)
	{
		AdminSession adminSession ;
		if(httpSession.getAttribute(ATTR_NAME)!=null && (httpSession.getAttribute(ATTR_NAME) instanceof AdminSession))
		{
			adminSession = AdminSession.class.cast(httpSession.getAttribute(ATTR_NAME));
			return adminSession;
		}
		else
			return null;
	}
	
	public static AdminSession createAdminSession(HttpSession httpSession , User user)
	{
		AdminSession adminSession = new AdminSession();
		adminSession.setLoggedInTime(new Date());
		adminSession.setUser(user);
		httpSession.setAttribute(ATTR_NAME, adminSession);
		return adminSession;
	}
	
}
