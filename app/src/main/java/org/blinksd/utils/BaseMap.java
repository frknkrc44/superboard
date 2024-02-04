package org.blinksd.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class BaseMap<K, V> {
    private final Map<K, V> map = new LinkedHashMap<>();

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return map.containsValue(value);
    }

    public void clear() {
        map.clear();
    }

    public void putAll(Map<K, V> other) {
        map.putAll(other);
    }

    public V get(K key) {
        return map.get(key);
    }

    public void put(K key, V value) {
        map.put(key, value);
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public List<K> keyList() {
        return new ArrayList<>(keySet());
    }

    public int indexOfKey(K key) {
        Iterator<K> iterator = map.keySet().iterator();

        for (int i = 0; iterator.hasNext(); i++) {
            if (iterator.next() == key) {
                return i;
            }
        }

        return -1;
    }

    public int size() {
        return map.size();
    }

    public K getKeyByIndex(int index) {
        Iterator<K> iterator = map.keySet().iterator();
        K key = null;

        for (int i = 0; iterator.hasNext(); i++) {
            if (i == index) {
                key = iterator.next();
                break;
            } else {
                iterator.next();
            }
        }

        return key;
    }

    public K getKeyByValue(V value) {
        Iterator<V> iterator = map.values().iterator();
        K key = null;

        for (int i = 0; iterator.hasNext(); i++) {
            if (value == iterator.next()) {
                key = getKeyByIndex(i);
                break;
            }
        }

        return key;
    }
}
