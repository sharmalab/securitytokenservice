package edu.emory.cci.bindaas.sts.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;


public class PersistentHashMap <K,V>  extends HashMap<K, V> {
private static final long serialVersionUID = -2804233468591293561L;

private File file;
private Type type = new TypeToken<HashMap<K,V>>(){}.getType();
public PersistentHashMap(File file) throws Exception
{
	this.file = file;
	load();
}

private void load() throws Exception
{
	if(file.exists() && file.isFile())
	{
		HashMap<K,V> newHashMap = GSONUtil.getGSONInstance().fromJson(new FileReader(file), type);
		if(newHashMap!=null)
		this.putAll(newHashMap);
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
			 	GSONUtil.getGSONInstance().toJson(this , type ,  new JsonWriter(fw));
			 	fw.close();
//			 	System.out.println(GSONUtil.getGSONInstance().toJson(this , type));
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
