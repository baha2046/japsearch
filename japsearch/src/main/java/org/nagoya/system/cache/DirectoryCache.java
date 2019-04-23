package org.nagoya.system.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.system.DirectorySystem;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DirectoryCache extends CaffeineCache<Path, List<DirectoryEntry>> {
    DirectoryCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(60)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .build(DirectorySystem::loadPath);
    }
}
