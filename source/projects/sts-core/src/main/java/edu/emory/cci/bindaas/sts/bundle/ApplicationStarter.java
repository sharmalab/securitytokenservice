package edu.emory.cci.bindaas.sts.bundle;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

import edu.emory.cci.bindaas.sts.rest.api.ISecurityTokenServiceCore;
import edu.emory.cci.bindaas.sts.rest.impl.SecurityTokenServiceCoreImpl;

public class ApplicationStarter {

	private SecurityTokenServiceCoreImpl securityTokenService;
	public SecurityTokenServiceCoreImpl getSecurityTokenService() {
		return securityTokenService;
	}

	public void setSecurityTokenService(
			SecurityTokenServiceCoreImpl securityTokenService) {
		this.securityTokenService = securityTokenService;
	}

	private String host;
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	private Integer port;
	private Log log = LogFactory.getLog(getClass());
	
	public void startup()
	{
		String publishUrl = String.format("http://%s:%s/securityTokenService", host , port);
		BundleContext context = Activator.getContext();
		
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("edu.emory.cci.sts.cxf.service.name", "Security Token Service");
		props.put("edu.emory.cci.sts.cxf.service.address",  publishUrl );
		context.registerService(ISecurityTokenServiceCore.class.getName(), securityTokenService, props);
	}
}
