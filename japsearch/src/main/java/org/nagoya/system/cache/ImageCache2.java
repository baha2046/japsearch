package org.nagoya.system.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class ImageCache2 extends CaffeineCache<URL, Image> {
    ImageCache2() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(300)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .build((k) -> null);
    }
}
