package org.nagoya.system.cache;

public enum MovieCacheInstance {
    INSTANCE;

    private static final MovieCache movieCache = new MovieCache();

    public static MovieCache cache() {
        return movieCache;
    }
}