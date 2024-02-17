package org.blinksd.utils;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ListedMap<K, V> {
    private final List<Pair<K, V>> entries = new ArrayList<>();

    public final boolean containsKey(K key) {
        return indexOfKey(key) >= 0;
    }

    public final boolean containsValue(V value) {
        return indexOfValue(value) >= 0;
    }

    public final void clear() {
        entries.clear();
    }

    public final void putAll(Map<K, V> other) {
        for (Map.Entry<K, V> entry : other.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public final V get(K key) {
        int index = indexOfKey(key);
        if (index >= 0) {
            return entries.get(index).second;
        }

        return null;
    }

    public final void put(K key, V value) {
        int index = indexOfKey(key);

        if (index >= 0) {
            entries.set(index, new Pair<>(key, value));
        } else {
            entries.add(new Pair<>(key, value));
        }
    }

    public final void remove(K key) {
        if (containsKey(key)) {
            entries.remove(indexOfKey(key));
        }
    }

    public final List<K> keyList() {
        List<K> keys = new ArrayList<>(size());

        for (Pair<K, V> entry : entries) {
            keys.add(entry.first);
        }

        return keys;
    }

    public final int indexOfKey(K key) {
        for (int i = 0; i < size(); i++) {
            Pair<K, V> entry = entries.get(i);
            if (entry.first.equals(key)) {
                return i;
            }
        }

        return -1;
    }

    public final int indexOfValue(V value) {
        for (int i = 0; i < size(); i++) {
            Pair<K, V> entry = entries.get(i);
            if (entry.second.equals(value)) {
                return i;
            }
        }

        return -1;
    }

    public final int size() {
        return entries.size();
    }

    public final K getKeyByIndex(int index) {
        if (index >= 0 && size() > index) {
            return entries.get(index).first;
        }

        return null;
    }

    public final K getKeyByValue(V value) {
        return getKeyByIndex(indexOfValue(value));
    }
}
