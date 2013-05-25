package edu.emory.cci.bindaas.sts.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;


public class PersistentHashMap <K,V>  extends HashMap<K, V> {
private static final long serialVersionUID = -2804233468591293561L;

private File file;
private Type type = new TypeToken<HashMap<K,V>>(){}.getType();
private Class<K> keyType ;
private Class<V> valueType ;

public PersistentHashMap(File file , Class<K> clazzKey , Class<V> classValue) throws Exception
{
	this.file = file;
	this.keyType = clazzKey;
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
			String keyString = entry.getKey();
			String valueString = entry.getValue().toString();
			K key = GSONUtil.getGSONInstance().fromJson(keyString, keyType);
			V value = GSONUtil.getGSONInstance().fromJson(valueString, valueType);
			super.put(key , value);
		}
	}
	
}

@Override
public synchronized void clear() {
	super.clear();
	file.delete();
}

@Override
public  synchronized  V get(Object key) {

	return super.get(key);
}

@Override
public  synchronized V put(K key, V value) {
	try {
		 
		 V oldVal = super.put(key, value);
		 try{
			 	FileWriter fw = new FileWriter(file);
			 	JsonObject jsonObject = new JsonObject();
			 	for(K k : keySet())
			 	{
			 		String keyString = null;
			 		if(! (k instanceof String))
			 		{
			 			keyString = GSONUtil.getGSONInstance().toJson(k , keyType);
			 		}
			 		else
			 		{
			 			keyString = k.toString();
			 		}
			 		
					JsonElement valueJson = GSONUtil.getGSONInstance().toJsonTree( value , valueType);
					jsonObject.add(keyString , valueJson);
			 	}
			 	fw.write(jsonObject.toString());
//			 	GSONUtil.getGSONInstance().toJson(this , type ,  new JsonWriter(fw));
			 	fw.close();
			 	
		 }
		 catch(Exception ee)
		 {
			 if(oldVal!=null)
				 super.put(key, oldVal);
			 else
				 super.remove(key);
		 }
		 return oldVal;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
	
}

@Override
public synchronized  void putAll(Map<? extends K, ? extends V> m) {
	for(K key : m.keySet())
	{
		put(key , m.get(key));
	}
	
}

@Override
public synchronized  V remove(Object key) {
	try {
		 
		 V oldVal = super.remove(key);
		 try{
			 	GSONUtil.getGSONInstance().toJson(this , type ,  new JsonWriter(new FileWriter(file)));
		 }
		 catch(Exception ee)
		 {
			 if(oldVal!=null)
				 super.put((K) key, oldVal);
		 }
		 return oldVal;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
}



}
