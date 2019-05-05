package org.nagoya.system.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.vavr.control.Option;
import org.nagoya.model.MovieV2;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MovieCache extends CaffeineCache<Path, Option<MovieV2>> {

    MovieCache() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(300)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .build(MovieV2::fromNfoFile);

    }

}
