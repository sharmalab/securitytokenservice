package edu.emory.cci.bindaas.sts.util;

public interface IPartialMap<V> {

	public V put(String key , V value);
	public V get(String key);
	public V remove(String key);
	public void clear();
}
