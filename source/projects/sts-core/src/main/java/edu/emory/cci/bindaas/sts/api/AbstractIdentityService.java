package edu.emory.cci.bindaas.sts.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.opensaml2.model.SAMLSigningKeyPair;
import edu.emory.cci.bindaas.sts.opensaml2.util.SecurityUtil;
import edu.emory.cci.bindaas.sts.service.IConfigurationManagerService;
import edu.emory.cci.bindaas.sts.util.GSONUtil;

public  abstract class  AbstractIdentityService implements IIdentityService {

	private SAMLSigningKeyPair keyPair;
	private IConfigurationManagerService configurationManager;
	private String keyPairFilename;
	private IdentityServiceRegistration serviceRegistration;
	private Log log = LogFactory.getLog(getClass());
	
	public IdentityServiceRegistration getServiceRegistration() {
		return serviceRegistration;
	}
	public void setServiceRegistration(
			IdentityServiceRegistration serviceRegistration) {
		this.serviceRegistration = serviceRegistration;
	}
	public String getKeyPairFilename() {
		return keyPairFilename;
	}
	public void setKeyPairFilename(String keyPairFilename) {
		this.keyPairFilename = keyPairFilename;
	}
	public IConfigurationManagerService getConfigurationManager() {
		return configurationManager;
	}
	public void setConfigurationManager(IConfigurationManagerService configurationManager) {
		this.configurationManager = configurationManager;
	}
	public SAMLSigningKeyPair getKeyPair() {
		return keyPair;
	}
	public void setKeyPair(SAMLSigningKeyPair keyPair) {
		this.keyPair = keyPair;
	}
	
	
	public void setup(IdentityServiceRegistration serviceRegistration) throws IdentityProviderException
	{
		this.serviceRegistration = serviceRegistration;
		try {
			createOrLoadSAMLSigningKeyPair(serviceRegistration.getId());
		} catch (Exception e) {
			log.error(e);
			throw new IdentityProviderException(serviceRegistration.getId(), e);
		}
	}
	
	public void cleanup() throws IdentityProviderException
	{
		try {
			configurationManager.clean(serviceRegistration.getId());
		} catch (IOException e) {
			log.error(e);
			throw new IdentityProviderException(serviceRegistration.getId(), e);
		}
	}
	
	protected void createOrLoadSAMLSigningKeyPair(String id) throws IOException, GeneralSecurityException
	{
		log.trace("Retrieving KeyPair for [" + id + "]");
		InputStream is = configurationManager.retrieve(id, keyPairFilename);
		if(is!=null)
		{
			String content = toString(is);
			SAMLSigningKeyPair keyPair = GSONUtil.getGSONInstance().fromJson(content, SAMLSigningKeyPair.class);
			if(keyPair!=null && keyPair.getPrivateKeyDerBytes()!=null && keyPair.getPublicKeyDerBytes()!=null)
			{
				this.keyPair = keyPair;
				log.trace("KeyPair found and set for [" + id + "]");
			}
			else
			{
				throw new IOException("Unable to load/parse SAMLSigningKeyPair from the filesystem");
			}
		}
		else
		{
			log.trace("KeyPair not found. Generating new for [" + id + "]");
			SAMLSigningKeyPair keyPair = SecurityUtil.generateKeyPair();
			String content = GSONUtil.getGSONInstance().toJson(keyPair);
			configurationManager.store(id, keyPairFilename, content);
			this.keyPair = keyPair;
			log.trace("KeyPair generated and saved for [" + id + "]");
		}
	}
	
	public IdentityServiceRegistration getRegistrationInfo() {
		return serviceRegistration;
	}
	
	public static String toString(InputStream in) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int bytesRead = -1;
		
		while((bytesRead = in.read(buffer)) > 0)
		{
			baos.write(buffer, 0, bytesRead);
		}
		
		baos.close();
		return (new String(baos.toByteArray()));
	}
	
}
