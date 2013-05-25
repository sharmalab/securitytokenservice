package edu.emory.cci.bindaas.sts.rest.impl;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import edu.emory.cci.bindaas.sts.util.StandardMimeType;

public class RestUtil {

	public static final String VENDOR = "Center for Comprehensive Informatics, Emory University";
	public static Response createSuccessResponse(String message)
	{
		return createResponse(Response.ok(message).type(StandardMimeType.TEXT.toString()));
	}
	
	public static Response createJsonResponse(String message)
	{
		return createResponse(Response.ok(message).type(StandardMimeType.JSON.toString()));
	}
	
	
	public static Response createSuccessResponse(String message,String mimeType)
	{
		mimeType = mimeType == null? StandardMimeType.TEXT.toString() : mimeType;
		return createResponse(Response.ok(message).type(mimeType));
	}
	
	public static Response createMimeResponse(byte[] inputData , String mimeType)
	{
		return createResponse(Response.ok().type(mimeType).entity(inputData));
	}
	
	public static Response createMimeResponse(InputStream inputStream , String mimeType)
	{
		return createResponse(Response.ok().type(mimeType).entity(inputStream));
	}
	
	public static Response createErrorResponse(String message)
	{
		return createResponse(Response.serverError().entity(message));
	}
	
	public static Response createResponse(String message, int code)
	{
		
	 	return createResponse(Response.status(code).entity(message));
	}
	
	public static Response createSuccessResponse(String message , Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok(message).type(StandardMimeType.TEXT.toString());
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public static Response createJsonResponse(String message, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok(message).type(StandardMimeType.JSON.toString());
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	
	public static Response createSuccessResponse(String message,String mimeType, Map<String,Object> headers)
	{
		
		mimeType = mimeType == null? StandardMimeType.TEXT.toString() : mimeType;
		
		ResponseBuilder builder = Response.ok(message).type(mimeType);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public static Response createMimeResponse(byte[] inputData , String mimeType, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok().type(mimeType).entity(inputData);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public static Response createMimeResponse(InputStream inputStream , String mimeType, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.ok().type(mimeType).entity(inputStream);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public static Response createErrorResponse(String message, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.serverError().entity(message);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		return createResponse(builder);
	}
	
	public static Response createResponse(String message, int code, Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.status(code).entity(message);
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		 	return createResponse(builder);
	}
	
	public static Response createResponse(int code, StandardMimeType mime,  Map<String,Object> headers)
	{
		ResponseBuilder builder = Response.status(code).type(mime.toString());
		for(String key : headers.keySet())
		{
			builder = builder.header(key, headers.get(key));
		}
		 	return createResponse(builder);
	}
	
	
	public static Response createResponse(ResponseBuilder builder)
	{
		builder = builder.header("Access-Control-Allow-Origin", "*");
		builder = builder.header("Vendor",VENDOR);
		return builder.build();
	}
	

}
