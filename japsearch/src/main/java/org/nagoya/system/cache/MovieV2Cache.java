package org.nagoya.system.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.collection.HashMap;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.gson.VavrGson;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nagoya.UtilCommon;
import org.nagoya.model.MovieV2;
import org.nagoya.model.dataitem.ActorV2;
import org.nagoya.model.xmlserialization.FutureTypeAdapter;
import org.nagoya.model.xmlserialization.PathTypeAdapter;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;

public class MovieV2Cache {

    private static final String cacheFileName = "movie_cache.ini";

    private static final MovieV2Cache INSTANCE = new MovieV2Cache();

    private final Gson gson;
    private HashMap<String, Tuple3<FileTime, MovieV2, Integer>> map;

    @Contract(pure = true)
    public static MovieV2Cache getInstance() {
        return INSTANCE;
    }

    private MovieV2Cache() {
        GsonBuilder builder = new GsonBuilder();
        VavrGson.registerAll(builder);
        builder.registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter());
        builder.registerTypeHierarchyAdapter(Future.class, new FutureTypeAdapter<ArrayList<ActorV2>>(new TypeToken<ArrayList<ActorV2>>() {
        }.getType()));

        // builder.registerTypeAdapterFactory(new FxThumbTypeAdapterFactory());
        this.gson = builder.create();

        if (Files.exists(Path.of(cacheFileName))) {
            Type type = new TypeToken<HashMap<String, Tuple3<FileTime, MovieV2, Integer>>>() {
            }.getType();
            this.map = this.gson.fromJson(UtilCommon.readStringFromFile(Path.of(cacheFileName)), type);
            // GUICommon.debugMessage(mtest.toString());
        } else {
            this.map = HashMap.empty();
        }

    }

    public void saveCacheFile() {

        UtilCommon.saveStringToFile(Path.of(cacheFileName), this.gson.toJson(this.map));

        //UtilCommon.saveStringToFile(Path.of(cacheFileName), this.gson.toJson(this.map));
    }

    public Option<MovieV2> loadFromCache(@NotNull Path path) {
        return this.map.get(path.toString())
                .filter(t -> t._1.equals(Try.of(() -> Files.getLastModifiedTime(path)).getOrNull()))
                .peek(t -> this.map = this.map.replaceValue(path.toString(), t.update3(t._3 + 1)))
                //.peek(t -> GUICommon.debugMessage(() -> "Load movie from cache " + path.toString()))
                .map(Tuple3::_2);
    }

    public void putCache(Path path, MovieV2 movieV2, FileTime fileTime) {
        if (movieV2 == null || fileTime == null) {
            return;
        }

        if (this.map.get(path.toString()).isDefined()) {
            this.removeCache(path);
        }
        this.map = this.map.put(path.toString(), Tuple.of(fileTime, movieV2, 0));
    }

    public void removeCache(Path path) {

        this.map = this.map.remove(path.toString());
    }

  /*  public static final class Adapter extends TypeAdapter<Tuple3<FileTime, MovieV2, Integer>> {
        private final Gson gson;

        public Adapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, Tuple3<FileTime, MovieV2, Integer> value) throws IOException {
            out.beginArray();
            this.gson.getAdapter(FileTime.class).write(out, value._1);
            this.gson.getAdapter(MovieV2.class).write(out, value._2);
            this.gson.getAdapter(Integer.class).write(out, value._3);
            out.endArray();
        }

        @Override
        public Tuple3<FileTime, MovieV2, Integer> read(JsonReader in) throws IOException {
            in.beginArray();
            //String path = in.nextString();
            FileTime p1 = this.gson.getAdapter(FileTime.class).read(in);
            MovieV2 p2 = this.gson.getAdapter(MovieV2.class).read(in);
            Integer p3 = this.gson.getAdapter(Integer.class).read(in);
            in.endArray();

            return Tuple.of(p1, p2, p3);
        }
    }

    public static final TypeAdapterFactory Factory = new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!type.getRawType().equals(Tuple3.class)) {
                return null;
            }

            TypeAdapter<T> casted = (TypeAdapter<T>) new Adapter(gson);

            return casted;
        }
    };
*/
}
