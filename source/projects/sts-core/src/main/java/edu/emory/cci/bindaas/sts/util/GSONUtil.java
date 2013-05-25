package edu.emory.cci.bindaas.sts.util;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GSONUtil {

	private static Gson gson;
	private static JsonParser jsonParser;
	
	private GSONUtil()
	{
		// do all the initialization here
	}
	
	
	public static JsonParser getJsonParser()
	{
		if(jsonParser == null)
		{
			jsonParser = new JsonParser();
		}
		return jsonParser;
	}
	public static Gson getGSONInstance()
	{
		if(gson == null)
		{
			GsonBuilder builder = new GsonBuilder();
			builder.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE);
			builder.registerTypeAdapter(Class.class, new ClassSerializer());
			builder.registerTypeAdapter(Class.class, new ClassDeserializer());
			builder.registerTypeAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
			builder.registerTypeAdapter(JsonObject.class, new JsonObjectSerializerDeserializer());
			
			gson = builder.excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
			
		}
		return gson;
	}
	
	private static class ClassSerializer implements JsonSerializer<Class<? extends Object>> {

		public JsonElement serialize(Class<? extends Object> clazz, Type arg1,
				JsonSerializationContext context) {
			
			return new JsonPrimitive(clazz.getName());
		}
		
	}
	private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decodeBase64(json.getAsString().getBytes());
        }
 
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(new String(Base64.encodeBase64(src)));
        }
    }

	public static class JsonObjectSerializerDeserializer implements JsonDeserializer<JsonObject> , JsonSerializer<JsonObject> {

		public JsonElement serialize(JsonObject src, Type typeOfSrc,
				JsonSerializationContext context) {
			
			return src;
		}

		
		public JsonObject deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			
			return json.getAsJsonObject();
		}

	}

	private static class ClassDeserializer implements JsonDeserializer<Class<? extends Object>> {

		public Class<? extends Object> deserialize(JsonElement json, Type arg1,
				JsonDeserializationContext arg2) throws JsonParseException {
			String val = json.getAsString();
			try {
				return Class.forName(val);
			} catch (ClassNotFoundException e) {
				
				e.printStackTrace();
				return null;
			}
			
		}
		
	}
}
