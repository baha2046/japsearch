package org.nagoya.system.cache;

public enum ImageCacheInstance {
    INSTANCE;

    private static final ImageCache2 imageCache = new ImageCache2();

    public static ImageCache2 cache() {
        return imageCache;
    }
}