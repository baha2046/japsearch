package org.nagoya.system.cache;

import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.function.Function;

public abstract class CaffeineCache<K, V> {
    protected LoadingCache<K, V> cache;

    public void put(K k, V v) {
        if (this.cache != null) {
            this.cache.put(k, v);
        }
    }

    public V get(K k) {
        if (this.cache != null) {
            return this.cache.get(k);
        } else {
            return null;
        }
    }

    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        if (this.cache != null) {
            return this.cache.get(key, mappingFunction);
        } else {
            return null;
        }
    }

    public void evict(K k) {
        if (this.cache != null) {
            this.cache.invalidate(k);
        }
    }

    public long size() {
        if (this.cache != null) {
            return this.cache.estimatedSize();
        } else {
            return 0;
        }
    }
}
