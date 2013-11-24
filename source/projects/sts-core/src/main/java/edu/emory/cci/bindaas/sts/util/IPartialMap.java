package edu.emory.cci.bindaas.sts.util;

import java.util.List;

public interface IPartialMap<V> {

	public V put(String key , V value);
	public V get(String key);
	public V remove(String key);
	public void clear();
	
	public List<V> values();
}
