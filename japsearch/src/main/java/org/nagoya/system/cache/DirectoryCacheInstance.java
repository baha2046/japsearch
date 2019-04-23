package org.nagoya.system.cache;

public enum DirectoryCacheInstance {
    INSTANCE;

    private static final DirectoryCache dirCache = new DirectoryCache();

    public static DirectoryCache cache() {
        return dirCache;
    }
}