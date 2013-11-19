package edu.emory.cci.bindaas.sts.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class PersistentHashMap <V> implements IPartialMap<V> , Serializable {

private static final long serialVersionUID = -2804233468591293561L;
private File file;
private Class<V> valueType ;

public PersistentHashMap(File file , Class<V> classValue) throws Exception
{
	this.file = file;
	this.valueType = classValue;
	load();
}

private void load() throws Exception
{
	if(file.exists() && file.isFile())
	{
		JsonObject json = GSONUtil.getJsonParser().parse(new FileReader(file)).getAsJsonObject();
		
		Set<Entry<String, JsonElement>> entrySet = json.entrySet();
		
		for(Entry<String, JsonElement> entry : entrySet)
		{
			String key = entry.getKey();
			String valueString = entry.getValue().toString();
			V value = GSONUtil.getGSONInstance().fromJson(valueString, valueType);
			put(key , value);
		}
	}
	else
	{
		FileWriter fw = new FileWriter(file);
		fw.append("{}");
		fw.close();
	}
	
}


public synchronized void clear() {
	file.delete();
}


public  synchronized  V get(String key) {

	try {
		JsonObject json = readFromFile();
		
		if(json.has(key))
		 {
			return GSONUtil.getGSONInstance().fromJson(json.get(key), valueType);
		 }
		else
			return null;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}


private synchronized JsonObject readFromFile() throws Exception
{
	FileReader reader = null;
	try{
		 reader = new FileReader(file);
		 JsonObject json = GSONUtil.getJsonParser().parse(reader).getAsJsonObject();
		 return json;
	}
	catch(Exception e)
	{
		throw e;
	}
	finally{
		if(reader!=null)
		{
			reader.close();
		}
	}
}

public  synchronized V put(String key, V value) {
	
	try {
		 V oldVal = null;
		
		 JsonObject json = readFromFile();
		 
		 if(json.has(key))
		 {
			 oldVal = GSONUtil.getGSONInstance().fromJson(json.get(key), valueType);
		 }
		 
		 FileWriter fw = new FileWriter(file);
		 json.add(key, GSONUtil.getGSONInstance().toJsonTree(value));
		 fw.write(json.toString());
		 fw.close();
		 
		 return oldVal;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
	
}



public synchronized  V remove(String key) {
	try {
		 
		 V oldVal = null;
		 JsonObject json = readFromFile();
		 
		 if(json.has(key))
		 {
			 oldVal = GSONUtil.getGSONInstance().fromJson(json.get(key), valueType);
		 }
		 
		 FileWriter fw = new FileWriter(file);
		 json.remove(key);
		 fw.write(json.toString());
		 fw.close();
		 
		 return oldVal;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}



}
