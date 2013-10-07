package edu.emory.cci.bindaas.sts.internal.web.view.openid;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.server.ServerManager;

import edu.emory.cci.bindaas.sts.api.model.User;

public class OpenIdHelper {
	  
	  private static Log log = LogFactory.getLog(OpenIdHelper.class);

	  /**
	   * 
	   * @param response
	   * @param request
	   * @throws IOException
	   */
	  public static Message processAssociationRequest(ServerManager serverManager ,  ParameterList request) throws IOException {
	  
	    Message message = serverManager.associationResponse(request);
	    return message;
	  
	  }

	  /**
	   * This is a helpful method to enable if you want to see what is being
	   * sent across. Disable this in production.
	   * 
	   * @param request
	   */

	  public static void logRequestParameters(ParameterList request) {
	    log.trace("logRequestParameters() BEGIN...");
	    if (log.isDebugEnabled()) {
	      log.debug("Dumping request parameters:");
	      List<Parameter> paramList = request.getParameters();
	      for (Parameter parameter : paramList) {
	        log.debug(parameter.getKey() + ":" + parameter.getValue());
	      }
	    }
	    log.trace("logRequestParameters() END...");
	  }

	 
	  
	  /**
	   * TODO: implement this using Velocity Template
	   * @return
	   */
	  public static String createXrdsResponse(String opEndpointUrl) {
	    log.trace("createXrdsResponse() BEGIN...");
//	    XrdsDocumentBuilder documentBuilder = new XrdsDocumentBuilder();
//	    documentBuilder.addServiceElement("http://specs.openid.net/auth/2.0/server", OpenIdProviderService.getOpEndpointUrl(), "10");
//	    documentBuilder.addServiceElement("http://specs.openid.net/auth/2.0/signon", OpenIdProviderService.getOpEndpointUrl(), "20");
//	    documentBuilder.addServiceElement(AxMessage.OPENID_NS_AX, OpenIdProviderService.getOpEndpointUrl(), "30");
//	    documentBuilder.addServiceElement(SRegMessage.OPENID_NS_SREG, OpenIdProviderService.getOpEndpointUrl(), "40");
	    
	    
	    log.trace("createXrdsResponse() BEGIN...");
	    return null;
	  }

	  

	  public static void sendDirectResponse(HttpServletResponse response, Message message) throws IOException {
	    log.trace("sendPlainTextResponse() BEGIN...");
	    response.setContentType("text/plain");
	    OutputStream os = response.getOutputStream();
	    os.write(message.keyValueFormEncoding().getBytes());
	    os.close();
	    log.trace("sendPlainTextResponse() END...");
	  }

	  /**
	   * This is where the bulk of the action happens. Once the handshaking is
	   * done and the OP is ready to send the response back to the requester, this
	   * method is used to pack it all up and get it ready to ship back.
	   * 
	   * See comments below for more details.
	   * 
	   * @param requestParameters
	   * @param userSelectedId
	   * @param userSelectedClaimedId
	   * @param registrationModel
	   * @return
	   */
	  public static Message buildAuthResponse(ServerManager serverManager , User user , ParameterList requestParameters, String userSelectedId, String userSelectedClaimedId , Set<Attribute>  userAuthorizedAttributes) {
	    log.trace("buildAuthResponse() BEGIN...");
	    Message authResponse = serverManager.authResponse(requestParameters, userSelectedId, userSelectedClaimedId, true);
	    // Check for and process any extensions the RP has asked for
	    AuthRequest authRequest = null;
	    try {
	      authRequest = AuthRequest.createAuthRequest(requestParameters, serverManager.getRealmVerifier());

	    if (authRequest.hasExtension(SRegMessage.OPENID_NS_SREG)) {
	        log.debug("Processing Simple Registration Extension request...");
	        MessageExtension extensionRequestObject = authRequest.getExtension(SRegMessage.OPENID_NS_SREG);
	        if (extensionRequestObject instanceof SRegRequest) {
	          SRegRequest sRegRequest = (SRegRequest)extensionRequestObject;
	          
	          Map<String,String> sRegAttributes = new HashMap<String, String>();
	          for(Attribute attr : userAuthorizedAttributes)
	          {
	        	  sRegAttributes.put(attr.getSregName(), attr.extractValue(user));
	          }
	          
	          SRegResponse sRegResponse = SRegResponse.createSRegResponse(sRegRequest, sRegAttributes );
	          
	          // Add the information to the AuthResponse message
	          authResponse.addExtension(sRegResponse);
	        } else {
	          log.error("Cannot continue processing Simple Registration Extension. The object returned from the AuthRequest (of type " + extensionRequestObject.getClass().getName() + ") claims to be correct, but is not of type " + SRegRequest.class.getName() + " as expected.");
	        }
	      }
	      if (authRequest.hasExtension(AxMessage.OPENID_NS_AX)) {
	        log.debug("Processing Attribute Exchange request...");
	        MessageExtension extensionRequestObject = authRequest.getExtension(AxMessage.OPENID_NS_AX);
	        FetchResponse fetchResponse = null;
	        
	        if (extensionRequestObject instanceof FetchRequest) {
	          FetchRequest axRequest = (FetchRequest)extensionRequestObject;
	          Map attributesRequested = axRequest.getAttributes();
	          fetchResponse = FetchResponse.createFetchResponse();
	          
	          for(Object alias : attributesRequested.keySet())
	          {
	        	  
	        	  String key = alias.toString();
	        	  String axSchema =  attributesRequested.containsKey(key)?  attributesRequested.get(key).toString() : null ;
	        	  if(axSchema!=null)
	        	  {
	        		  Attribute attr = Attribute.lookupByAxSchema(axSchema);
	        		  if(attr!=null && userAuthorizedAttributes.contains(attr))
	        		  {
	        			  fetchResponse.addAttribute(key, axSchema, attr.extractValue(user));
	        		  }
	        	  }
	          }
	          
	          authResponse.addExtension(fetchResponse);
	        } else {
	          log.error("Cannot continue processing Attribute Exchange (AX) request. The object returned from the AuthRequest (of type " + extensionRequestObject.getClass().getName() + ") claims to be correct, but is not of type " + AxMessage.class.getName() + " as expected.");
	        }
	      }
	      serverManager.sign((AuthSuccess)authResponse);

	    } catch (Exception e) {
	      log.error("Error occurred creating AuthRequest object:", e);
	    }
	    log.trace("buildAuthResponse() End...");
	    return authResponse;
	  }
	  
	  /**
	   * return true if current ParameterList is seeking subset of information seeked in target
	   * @param current
	   * @param target
	   * @return
	 * @throws MessageException 
	   */
	  public static boolean isSubset(ServerManager serverManager , ParameterList current , ParameterList target) throws MessageException
	  {
		  Map<Attribute,Boolean> currentAttributes = getAttributeRequested(serverManager, current);
		  Map<Attribute,Boolean> targetAttributes = getAttributeRequested(serverManager, target);
		  
		  if(targetAttributes.keySet().containsAll(currentAttributes.keySet()))
		  {
			  return true;
		  }
		  else
			  return false; 
	  }
	  
	  
	  public static Map<Attribute,Boolean> getAttributeRequested(ServerManager serverManager , ParameterList parameterList) throws MessageException
	  {
		  Map<Attribute,Boolean> attributes = new HashMap<Attribute,Boolean>();
		  Set<Attribute> optionalAttributes = new HashSet<Attribute>();
		  Set<Attribute> mandatoryAttributes = new HashSet<Attribute>();
		  AuthRequest authRequest = AuthRequest.createAuthRequest(parameterList, serverManager.getRealmVerifier());
		  if (authRequest.hasExtension(SRegMessage.OPENID_NS_SREG)) {
		        log.debug("Processing Simple Registration Extension request...");
		        MessageExtension extensionRequestObject = authRequest.getExtension(SRegMessage.OPENID_NS_SREG);
		        if (extensionRequestObject instanceof SRegRequest) {
		          SRegRequest sRegRequest = (SRegRequest)extensionRequestObject;
		          
		          List mandatory = sRegRequest.getAttributes(true);
		          List optional = sRegRequest.getAttributes(false);
		          
		          for(Object optAttr : optional)
		          {
		        	  optionalAttributes.add(Attribute.lookupBySreg(optAttr.toString()));
		          }
		          
		          for(Object mandAttr : mandatory)
		          {
		        	  mandatoryAttributes.add(Attribute.lookupBySreg(mandAttr.toString()));
		          }
		          
		        } else {
		          log.error("Cannot continue processing Simple Registration Extension. The object returned from the AuthRequest (of type " + extensionRequestObject.getClass().getName() + ") claims to be correct, but is not of type " + SRegRequest.class.getName() + " as expected.");
		        }
		      }
		      if (authRequest.hasExtension(AxMessage.OPENID_NS_AX)) {
		        log.debug("Processing Attribute Exchange request...");
		        MessageExtension extensionRequestObject = authRequest.getExtension(AxMessage.OPENID_NS_AX);
		        
		        if (extensionRequestObject instanceof FetchRequest) {
		          FetchRequest axRequest = (FetchRequest)extensionRequestObject;
		          
		          Map mandatory = axRequest.getAttributes(true);
		          Map optional = axRequest.getAttributes(false);
		          
		          for(Object optAttr : optional.values())
		          {
		        	  optionalAttributes.add(Attribute.lookupBySreg(optAttr.toString()));
		          }
		          
		          for(Object mandAttr : mandatory.values())
		          {
		        	  mandatoryAttributes.add(Attribute.lookupBySreg(mandAttr.toString()));
		          }
		        } else {
		          log.error("Cannot continue processing Attribute Exchange (AX) request. The object returned from the AuthRequest (of type " + extensionRequestObject.getClass().getName() + ") claims to be correct, but is not of type " + AxMessage.class.getName() + " as expected.");
		        }
		      }
		      
		      for(Attribute attr : optionalAttributes)
		      {
		    	  attributes.put(attr, false);
		      }
		      
		      for(Attribute attr : mandatoryAttributes)
		      {
		    	  attributes.put(attr, true);
		      }
		  
		  return attributes;
	  }
	  

	}
