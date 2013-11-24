package edu.emory.cci.bindaas.sts.internal.web.action.dashboard;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import edu.emory.cci.bindaas.sts.internal.web.GeneralServlet;
import edu.emory.cci.bindaas.sts.internal.web.action.dashboard.api.IBodyCenterSection;
import edu.emory.cci.bindaas.sts.internal.web.action.dashboard.api.IBodyLeftMarginSection;
import edu.emory.cci.bindaas.sts.internal.web.action.dashboard.api.IBodyRightMarginSection;
import edu.emory.cci.bindaas.sts.internal.web.action.dashboard.api.IPageSubTitleSection;
import edu.emory.cci.bindaas.sts.internal.web.action.dashboard.api.IPageTitleSection;
import edu.emory.cci.bindaas.sts.internal.web.action.dashboard.api.IUserSection;
import edu.emory.cci.bindaas.sts.util.VelocityEngineWrapper;

public  class GeneralBaseTemplateServlet extends GeneralServlet {

	private static final long serialVersionUID = 9076379937346198341L;
	public  String getUserSection(HttpServletRequest request , HttpServletResponse response ) throws Exception
	{
		return userSection.getUserSection(request, response);
	}
	public  String getPageTitleSection(HttpServletRequest request , HttpServletResponse response ) throws Exception
	{
		return pageTitleSection.getPageTitleSection(request, response);
	}
	public  String getPageSubTitleSection(HttpServletRequest request , HttpServletResponse response ) throws Exception
	{
		return pageSubtitleSection.getPageSubTitleSection(request, response);
	}
	public  String getBodyLeftMarginSection(HttpServletRequest request , HttpServletResponse response ) throws Exception
	{
		return bodyLeftMarginSection.getBodyLeftMarginSection(request, response);
	}
	public  String getBodyCenterSection(HttpServletRequest request , HttpServletResponse response ) throws Exception
	{
		return bodyCenterSection.getBodyCenterSection(request, response);
	}
	public  String getBodyRightMarginSection(HttpServletRequest request , HttpServletResponse response ) throws Exception
	{
		return bodyRightMarginSection.getBodyRightMarginSection(request, response);
	}

	private VelocityEngineWrapper velocityEngineWrapper;
	private Template baseTemplate;
	private String baseTemplateName;
	
	private IBodyCenterSection bodyCenterSection;
	private IBodyLeftMarginSection bodyLeftMarginSection;
	private IBodyRightMarginSection bodyRightMarginSection;
	private IPageSubTitleSection pageSubtitleSection;
	private IPageTitleSection pageTitleSection;
	private IUserSection userSection;
	
	
	private Log log = LogFactory.getLog(getClass());
	
	public void init() throws ServletException
	{
		super.init();
		baseTemplate = velocityEngineWrapper.getVelocityTemplateByName(baseTemplateName);
	}
	
	/**
	 * Use this method to get base template with version information
	 * @return
	 * @throws ParseException
	 */
	public Template getFilledTemplate() throws ParseException
	{
		RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();            
        StringReader reader = new StringReader("");
        SimpleNode node = runtimeServices.parse(reader, "FilledBaseTemplate.vm");
        Template template = new Template();
        template.setRuntimeServices(runtimeServices);
        template.setData(node);
        template.initDocument();
        return template;
	}
	public void writeContent(HttpServletRequest request , HttpServletResponse response ) throws Exception
	{
		String userInformationSection = getUserSection(request,response);
		String pageTitleSection = getPageTitleSection(request,response);
		String pageSubTitleSection = getPageSubTitleSection(request,response);
		String bodyLeftMarginSection = getBodyLeftMarginSection(request,response);
		String bodyCenterSection = getBodyCenterSection(request,response);
		String bodyRightSection = getBodyRightMarginSection(request,response);
		
		if(bodyCenterSection == null || userInformationSection == null || pageTitleSection == null
				|| pageSubTitleSection == null || bodyLeftMarginSection == null || bodyRightSection == null)
			return;
		
		VelocityContext context = velocityEngineWrapper.createVelocityContext();
		context.put("userInformaation", userInformationSection);
		context.put("pageTitle",  pageTitleSection);
		context.put("pageSubTitle", pageSubTitleSection);
		context.put("bodyLeftMargin", bodyLeftMarginSection);
		context.put("bodyCenter",  bodyCenterSection);
		context.put("bodyRightMargin", bodyRightSection);
		baseTemplate.merge(context, response.getWriter());
	}
	
	public VelocityEngineWrapper getVelocityEngineWrapper() {
		return velocityEngineWrapper;
	}
	public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
		this.velocityEngineWrapper = velocityEngineWrapper;
	}
	public Template getBaseTemplate() {
		return baseTemplate;
	}
	public void setBaseTemplate(Template baseTemplate) {
		this.baseTemplate = baseTemplate;
	}
	public String getBaseTemplateName() {
		return baseTemplateName;
	}
	public void setBaseTemplateName(String baseTemplateName) {
		this.baseTemplateName = baseTemplateName;
	}
	public IBodyCenterSection getBodyCenterSection() {
		return bodyCenterSection;
	}
	public void setBodyCenterSection(IBodyCenterSection bodyCenterSection) {
		this.bodyCenterSection = bodyCenterSection;
	}
	public IBodyLeftMarginSection getBodyLeftMarginSection() {
		return bodyLeftMarginSection;
	}
	public void setBodyLeftMarginSection(
			IBodyLeftMarginSection bodyLeftMarginSection) {
		this.bodyLeftMarginSection = bodyLeftMarginSection;
	}
	public IBodyRightMarginSection getBodyRightMarginSection() {
		return bodyRightMarginSection;
	}
	public void setBodyRightMarginSection(
			IBodyRightMarginSection bodyRightMarginSection) {
		this.bodyRightMarginSection = bodyRightMarginSection;
	}
	public IPageSubTitleSection getPageSubtitleSection() {
		return pageSubtitleSection;
	}
	public void setPageSubtitleSection(IPageSubTitleSection pageSubtitleSection) {
		this.pageSubtitleSection = pageSubtitleSection;
	}
	public IPageTitleSection getPageTitleSection() {
		return pageTitleSection;
	}
	public void setPageTitleSection(IPageTitleSection pageTitleSection) {
		this.pageTitleSection = pageTitleSection;
	}
	public IUserSection getUserSection() {
		return userSection;
	}
	public void setUserSection(IUserSection userSection) {
		this.userSection = userSection;
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			writeContent(request, response);
		} catch (Exception e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			writeContent(request, response);
		} catch (Exception e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	
	
}
