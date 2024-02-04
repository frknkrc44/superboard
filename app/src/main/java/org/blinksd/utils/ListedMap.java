package org.blinksd.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ListedMap<K, V> {
    private final List<ListEntry<K, V>> entries = new ArrayList<>();

    public boolean containsKey(K key) {
        for (ListEntry<K, V> entry : entries) {
            if (entry.key.equals(key)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsValue(V value) {
        for (ListEntry<K, V> entry : entries) {
            if (entry.value.equals(value)) {
                return true;
            }
        }

        return false;
    }

    public void clear() {
        entries.clear();
    }

    public void putAll(Map<K, V> other) {
        for (Map.Entry<K, V> entry : other.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public V get(K key) {
        for (ListEntry<K, V> entry : entries) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }

        return null;
    }

    public void put(K key, V value) {
        if (!containsKey(key)) {
            entries.add(new ListEntry<>(key, value));
            return;
        }

        for (int i = 0; i < entries.size(); i++) {
            ListEntry<K, V> entry = entries.get(i);
            if (entry.key.equals(key)) {
                entries.remove(entry);
                entries.add(i, new ListEntry<>(key, value));
            }
        }
    }

    public List<K> keyList() {
        List<K> keys = new ArrayList<>(entries.size());

        for (ListEntry<K, V> entry : entries) {
            keys.add(entry.key);
        }

        return keys;
    }

    public int indexOfKey(K key) {
        for (int i = 0; i < entries.size(); i++) {
            ListEntry<K, V> entry = entries.get(i);
            if (entry.key.equals(key)) {
                return i;
            }
        }

        return -1;
    }

    public int size() {
        return entries.size();
    }

    public K getKeyByIndex(int index) {
        if (entries.size() > index) {
            return entries.get(index).key;
        }

        return null;
    }

    public K getKeyByValue(V value) {
        for (int i = 0; i < entries.size(); i++) {
            ListEntry<K, V> entry = entries.get(i);
            if (entry.value.equals(value)) {
                return entry.key;
            }
        }

        return null;
    }

    private static class ListEntry<K, V> {
        final K key;
        final V value;

        private ListEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
