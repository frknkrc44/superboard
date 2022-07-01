package org.blinksd.utils.layout;

import java.util.*;

public class BaseMap<K, V> {
	private Map<K, V> map = new LinkedHashMap<>();
	
	public boolean containsKey(K key){
		return map.containsKey(key);
	}

	public V get(K key){
		return map.get(key);
	}

	public void put(K key, V value){
		map.put(key, value);
	}
	
	public Set<K> keySet(){
		return map.keySet();
	}
	
	public List<K> keyList(){
		return new ArrayList<K>(keySet());
	}
	
	public int indexOf(K theme){
		return keyList().indexOf(theme);
	}
	
	public int size(){
		return map.size();
	}
	
	public K getFromIndex(int index){
		List<K> keys = keyList();
		return keys.get(keys.size() > index ? index : 0);
	}
}
