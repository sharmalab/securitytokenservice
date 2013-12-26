package edu.emory.cci.bindaas.sts.cagrid.ext;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;





import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import edu.emory.cci.bindaas.sts.api.AbstractIdentityService;
import edu.emory.cci.bindaas.sts.api.exception.AuthenticationException;
import edu.emory.cci.bindaas.sts.api.exception.IdentityProviderException;
import edu.emory.cci.bindaas.sts.api.exception.MethodNotImplementedException;
import edu.emory.cci.bindaas.sts.api.model.Credential;
import edu.emory.cci.bindaas.sts.api.model.Group;
import edu.emory.cci.bindaas.sts.api.model.IdentityServiceRegistration;
import edu.emory.cci.bindaas.sts.api.model.SecureToken;
import edu.emory.cci.bindaas.sts.api.model.ServiceOperation;
import edu.emory.cci.bindaas.sts.api.model.User;

public class DorianGrouperIdentityService  extends AbstractIdentityService{

private static Set<ServiceOperation> allowedOperations;
	
	static {
		allowedOperations = new HashSet<ServiceOperation>();
		allowedOperations.add(ServiceOperation.issueToken);
		allowedOperations.add(ServiceOperation.validateToken);
		allowedOperations.add(ServiceOperation.getUsers);
		allowedOperations.add(ServiceOperation.getGroups);
	}
	
	
	
	
	private Log log = LogFactory.getLog(getClass());
	private DorianGrouperConfiguration configuration;
	
	
	public void setup(IdentityServiceRegistration serviceRegistration) throws IdentityProviderException
	{
		configuration = DorianGrouperConfiguration.convert(serviceRegistration.getConfiguration());
		try {
			configuration.validate();
			setServiceRegistration(serviceRegistration);
			
		} catch (Exception e) {
			log.error(e);
			throw new IdentityProviderException(DorianGrouperIdentityProvider.class.getName() , e);
		}
		
		
	}
	
	
	public boolean authenticate(Credential credential) throws IdentityProviderException{
		if(credential.getUsername()!=null && credential.getPassword()!=null )
		{
			// authenticate
			try{
			DefaultHttpClient httpClient = getHttpClient(configuration, credential.getUsername(), credential.getPassword());
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("targetService", "http://services.testcorp.org/provider1"));
            String queryString = URLEncodedUtils.format(qparams, "UTF-8");
            String httpGetRequestUrl = String.format("%s/issueToken?%s", configuration.getBaseSTSUrl() , queryString);

            log.debug("Executing HttpGet on [" + httpGetRequestUrl + "]");
            HttpResponse response =  httpClient.execute(new HttpGet(httpGetRequestUrl));
            HttpEntity entity = response.getEntity();
            if (entity != null) {
						log.debug(EntityUtils.toString(entity));
						return true;
            }
			}catch(Exception e)
			{
				throw new IdentityProviderException(DorianGrouperIdentityProvider.class.getName(), e);
			}
		}
		return false;
	}
	
	private DefaultHttpClient getHttpClient(DorianGrouperConfiguration configuration , String username , String password) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
	{
		SSLSocketFactory sf = new SSLSocketFactory(new TrustStrategy() {

	        public boolean isTrusted(
	                final X509Certificate[] chain, String authType) throws CertificateException {
	            return true;
	        }

	    });
		Scheme httpsScheme = new Scheme("https", 443, sf);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(httpsScheme);

		// apache HttpClient version >4.2 should use BasicClientConnectionManager
		ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
		
		DefaultHttpClient httpclient = new DefaultHttpClient(cm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username,password);
        List<String> authpref = new ArrayList<String>();
        authpref.add(AuthPolicy.BASIC);
        authpref.add(AuthPolicy.DIGEST);
        httpclient.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authpref);
        HttpHost targetHost = new HttpHost(configuration.getHost(), configuration.getPort(), configuration.getProtocol());
        httpclient.getCredentialsProvider().setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), creds);
        
        return httpclient;

	}
	
	public SecureToken issueToken(Credential credential , String serviceProvider)
			throws IdentityProviderException, AuthenticationException {
		
		if(credential.getUsername()!=null && credential.getPassword()!=null )
		{
			// authenticate and return SecureToken
			try {
				DefaultHttpClient httpClient = getHttpClient(configuration,
						credential.getUsername(), credential.getPassword());
				List<NameValuePair> qparams = new ArrayList<NameValuePair>();
				qparams.add(new BasicNameValuePair("targetService",
						"http://services.testcorp.org/provider1"));
				String queryString = URLEncodedUtils.format(qparams, "UTF-8");
				String httpGetRequestUrl = String.format("%s/issueToken?%s",
						configuration.getBaseSTSUrl(), queryString);

				log.debug("Executing HttpGet on [" + httpGetRequestUrl + "]");
				HttpResponse response = httpClient.execute(new HttpGet(
						httpGetRequestUrl));
				HttpEntity entity = response.getEntity();
				if (entity != null) {

					SecureToken token = new SecureToken(
							EntityUtils.toString(entity));
					return token;
				}
				else
					throw new AuthenticationException(DorianGrouperIdentityProvider.class.getName());
			} catch (Exception e) {
				throw new AuthenticationException(
						DorianGrouperIdentityProvider.class.getName());
			}
		}
		else
			throw new AuthenticationException(getServiceRegistration().getId());
	}

	public boolean validateToken(SecureToken secureToken)
			throws IdentityProviderException {
		IdentityServiceRegistration serviceReg = getServiceRegistration();
		try {
			
			DefaultHttpClient httpClient = getHttpClient(configuration,
					"UserA", "PassA");
			
			String validateToken = String.format("%s/validateToken" , configuration.getBaseSTSUrl());
			String tokenString = secureToken.getContent();
			HttpPost validateRequest = new HttpPost(validateToken);

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("token", tokenString));

            UrlEncodedFormEntity formentity = new UrlEncodedFormEntity(formparams, "UTF-8");

            validateRequest.setEntity(formentity);
            HttpResponse serverresponse =  httpClient.execute(validateRequest);
            
            HttpEntity entity = serverresponse.getEntity();
            if (entity != null) {
                   String resp  = EntityUtils.toString(entity);
                   
                   if(resp!=null && resp.equals("http://docs.oasis-open.org/ws-sx/ws-trust/200512/status/valid")) return true;
                   else
                	   return false;

            } return false;
			
		}catch(Exception e)
		{
			log.error(e);
			throw new IdentityProviderException(serviceReg.getId() , e);
		}
	}

	public Collection<User> getUsers() throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.getUsers);
	}

	public Collection<Group> getGroups() throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.getGroups);
	}

	public User addUser(String username, Collection<String> groups)
			throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.addUser);
		
	}

	public Group addGroup(String group, Collection<String> users)
			throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.addGroup);
	}

	public Group removeGroup(String group) throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.removeGroup);
	}

	public User removeUser(String user) throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.removeUser);
	}

	public Group updateGroup(Group group) throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.updateGroup);
	}

	public User updateUser(User user) throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.updateUser);
	}

	public boolean isOperationSupported(ServiceOperation serviceOperation) {
		return allowedOperations.contains(serviceOperation);
	}



	public User lookupUserByName(String username)
			throws IdentityProviderException {
		throw new MethodNotImplementedException(DorianGrouperIdentityProvider.class.getName(), ServiceOperation.lookupUserByName);
	}


}
