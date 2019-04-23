package org.nagoya.model;

import cyclops.control.Try;
import io.vavr.CheckedFunction0;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.model.dataitem.ActorV2;
import org.nagoya.model.dataitem.FxThumb;
import org.nagoya.model.dataitem.Title;
import org.nagoya.preferences.GeneralSettings;
import org.nagoya.preferences.RenameSettings;
import org.nagoya.system.Benchmark;
import org.nagoya.system.DirectorySystem;
import org.nagoya.system.Systems;
import org.nagoya.system.cache.IconCache;
import org.nagoya.system.cache.MovieCacheInstance;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class DirectoryEntry implements CheckedFunction0<Boolean>, Runnable {

    private static final long serialVersionUID = -2799720372564283731L;

    private final Path filePath;

    private final boolean directory;

    private final Path dirPath;

    private final MoviePaths moviePaths = new MoviePaths();

    private final BooleanProperty needCheckProperty = new SimpleBooleanProperty(true);

    private final StringProperty fileNameProperty = new SimpleStringProperty("");

    private final StringProperty fileExtenionProperty = new SimpleStringProperty("");

    protected Option<MovieV2> movieData = Option.none();

    private Image fileIcon;

    private long size = 0;

    protected DirectoryEntry() {
        this.filePath = null;
        this.dirPath = null;
        this.directory = true;

        this.needCheckProperty.setValue(false);
        this.fileExtenionProperty.set("Folder");
    }

    public DirectoryEntry(@NotNull Path inPath) {
        this.needCheckProperty.setValue(true);
        this.fileNameProperty.set(inPath.getFileName().toString());

        this.filePath = inPath;
        this.directory = Files.isDirectory(inPath);

        if (!this.isDirectory()) {
            this.dirPath = inPath.getParent();
            this.fileNameProperty.set(FilenameUtils.removeExtension(this.fileNameProperty.get()));
            this.fileExtenionProperty.set("File");
        } else {
            this.dirPath = inPath;
            this.fileExtenionProperty.set("Folder");
        }

        try {
            ImageIcon ic = (ImageIcon) IconCache.getIconFromCache(inPath.toFile());

            BufferedImage image = (BufferedImage) ic.getImage();
            this.fileIcon = SwingFXUtils.toFXImage(image, null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    // standard constructors/getters

    @NotNull
    @Contract("_ -> new")
    public static DirectoryEntry of(Path inPath) {
        return new DirectoryEntry(inPath);
    }

    public static DirectoryEntry getAndExe(Path inPath) {
        DirectoryEntry directoryEntry = new DirectoryEntry(inPath);
        Systems.useExecutors(directoryEntry);
        return directoryEntry;
    }

    @NotNull
    @Contract(pure = true)
    public static Callback<DirectoryEntry, Observable[]> extractor() {
        return (DirectoryEntry p) -> new Observable[]{p.getNeedCheckProperty()};
    }

    private static Option<Path> getNfoFilePath(@NotNull Vector<Path> dirPath) {
        return dirPath.find(p -> p.getFileName().toString().endsWith(".nfo"));
    }

    private static Vector<Tuple2<Path, Long>> getMovieFilePaths(Vector<Path> dirPath) {
        Vector<String> movExt = Vector.of(MovieFilenameFilter.acceptedMovieExtensions);

        return dirPath.filter(p -> movExt.find(ext -> p.getFileName().toString().toLowerCase().endsWith(ext)).isDefined())
                //.peek(p->GUICommon.debugMessage(p.getFileNameProperty().toString()))
                .map(p -> Tuple.of(p, (Try.withCatch(() -> Files.size(p) / 1024 / 1024, IOException.class).orElse(0L))));
    }

    private static Future<Stream<FxThumb>> tryLoadExtraImage(Path extraImagePath) {

        return Future.of(Systems.getExecutorServices(),
                () -> DirectorySystem.readPath(extraImagePath)
                        .filter(Files::isRegularFile)
                        .filter(f -> f.getFileName().toString().endsWith(".jpg"))
                        .map(FxThumb::of).toStream());
    }

    private static void tryLoadLocalActorImage(Path actorImagePath, List<ActorV2> actorList) {

        Vector<Path> paths = DirectorySystem.readPath(actorImagePath);

        actorList.forEach(a -> paths.find(p -> FileSystems.getDefault().getPathMatcher("glob:"
                        + a.getName().replace(' ', '_') + "*").matches(p.getFileName()))
                        .peek(p -> a.setLocalImage(FxThumb.of(p)))
                //.peek(p -> GUICommon.debugMessage(p.getFileNameProperty().toString())));
        );
    }

    public Boolean DoCheckFile(boolean needCheck) {
        if (needCheck) {

            Benchmark b = new Benchmark(false, "DirectorEntry - " + this.filePath.getFileName().toString());

            // system.out.println("CHECKING~~~~~~~~~~~~~~~~ ");
             /*
              Check if movie exists
             */
            String strExtension = null;

            if (!this.isDirectory()) {
                try {
                    this.size = Files.size(this.filePath) / 1024 / 1024;
                    strExtension = Files.probeContentType(this.filePath);
                    if (strExtension == null) {
                        InputStream is = new BufferedInputStream(new FileInputStream(this.filePath.toFile()));
                        strExtension = URLConnection.guessContentTypeFromStream(is);
                        is.close();
                    }
                    String ext = strExtension;
                    Platform.runLater(() -> this.fileExtenionProperty.set(ext));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            b.B("1> ");

            Vector<Path> dirFileList = DirectorySystem.readPath(this.dirPath);

            b.B("2> Read P ");

            if (this.isDirectory() || (strExtension != null && strExtension.contains("video"))) {
                this.moviePaths.setMovies(getMovieFilePaths(dirFileList));

                if (this.isDirectory()) {
                    this.size = this.moviePaths.getMovies().map(Tuple2::_2).sum().longValue();
                }

                if (this.moviePaths.getMovies().length() > 10) {
                    this.moviePaths.setMovies(Vector.empty());
                }
            }

            b.B("3> After M ");

            if (this.hasMovie()) {
                String movName = MovieV2.getUnstackedMovieName(this.moviePaths.getMovies().get()._1());
                this.moviePaths.resolvePaths(this.dirPath, movName);
                this.moviePaths.nfoPath = getNfoFilePath(dirFileList).getOrNull();

                //system.out.println("Thread End >>this.logicalPath_Poster	 " + this.logicalPath_Poster.getFileNameProperty().toString());

                b.B("4> After NFO ");

                if (this.hasNfo()) {
                    this.movieData = Option.of(MovieCacheInstance.cache().get(this.moviePaths.nfoPath));//this.readMovieFromNfoFile(this.nfoPath);

                    if (this.movieData.isEmpty()) {
                        this.moviePaths.nfoPath = null;
                    } else {

                        File poster = this.movieData.flatMap(MovieV2::getImgBackCover)
                                .filter(FxThumb::isLocal)
                                .flatMap(FxThumb::getLocalPath)
                                .getOrElse(this.moviePaths.posterPath.toFile());

                        b.B("5> Before P C ");

                        if (poster.exists()) {
                            this.movieData.flatMap(MovieV2::getImgBackCover).peek((t) -> t.getImageLocal(poster, (i) -> {
                            }));
                        } else {
                            this.movieData.get().setImgBackCover(Option.none());
                        }

                        if (Files.exists(this.moviePaths.coverPath)) {
                            //GUICommon.debugMessage("Files.exists");
                            this.movieData.flatMap(MovieV2::getImgFrontCover).peek((t) -> t.getImageLocal(this.moviePaths.coverPath.toFile(), (i) -> {
                            }));
                        }

                        b.B("6> After P C ");


                        if (Files.exists(this.moviePaths.actorFolderPath)) {
                            tryLoadLocalActorImage(this.moviePaths.actorFolderPath, this.movieData.get().getActorList());
                        }

                        if (Files.exists(this.moviePaths.extraArtFolderPath)) {
                            tryLoadExtraImage(this.moviePaths.extraArtFolderPath).onSuccess(this.movieData.get()::setImgExtras);
                        }

                    }
                }
            }

            b.B("F ");

            Platform.runLater(() -> this.setNeedCheck(false));
        }

        return true;
    }

    public void clearCache() {

        this.movieData.peek(MovieV2::clearCache);

        if (this.hasNfo()) {
            //  system.out.println("Debug MovieCacheInstance B" + MovieCacheInstance.cache().size());
            MovieCacheInstance.cache().evict(this.moviePaths.nfoPath);
            //system.out.println("Debug MovieCacheInstance A" + MovieCacheInstance.cache().size());
        }
        this.moviePaths.clear();
        this.setNeedCheck(true);
    }

    public void writeMovieDataToFile(Option<ObservableList<String>> outText, Runnable runWhenFinish) {

        MovieV2 MovieToSave = this.getMovieData();

        if (!MovieToSave.hasValidTitle()) {
            System.out.println("No match for this movie in the array or there was no title filled in; skipping writing");
            return;
        }

        GUICommon.writeToObList("=======================================================", outText);
        GUICommon.writeToObList("Writing movie: " + this.movieData.map(MovieV2::getMovieTitle).map(Title::getTitle).get(), outText);
        GUICommon.writeToObList("=======================================================", outText);

        GeneralSettings preferences = GUICommon.getPreferences();

        Systems.useExecutors(() -> {

            if (preferences.getRenameMovieFile()) {
                String movID = this.movieData.map(RenameSettings::getSuitableFileName).get();

                try {
                    int Naming = 1;
                    for (Path p : this.moviePaths.getMovies().map(Tuple2::_1)) {
                        String ext = FilenameUtils.getExtension(p.getFileName().toString());
                        Path idealPath;
                        if (this.moviePaths.getMovies().size() > 1) {
                            idealPath = this.dirPath.resolve(movID + " pt" + Naming + "." + ext);
                            Naming++;
                        } else {
                            idealPath = this.dirPath.resolve(movID + "." + ext);
                        }
                        GUICommon.writeToObList(">> " + idealPath.getFileName(), outText);
                        Files.move(p, idealPath);
                    }

                    if (this.hasNfo()) {
                        Files.deleteIfExists(this.moviePaths.nfoPath);
                        Files.deleteIfExists(this.moviePaths.posterPath);
                        Files.deleteIfExists(this.moviePaths.coverPath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.moviePaths.nfoPath = this.dirPath.resolve(Movie.getFileNameOfNfo(movID, preferences.getNfoNamedMovieDotNfo()));
                this.moviePaths.resolvePaths(this.dirPath, movID);
            }

            try {
                MovieToSave.writeToFile(
                        this.moviePaths.getNfoPath(),
                        this.moviePaths.getPosterPath(),
                        this.moviePaths.getCoverPath(),
                        this.moviePaths.getFolderImagePath(),
                        this.moviePaths.getActorFolderPath(),
                        this.moviePaths.getExtraArtFolderPath(),
                        this.moviePaths.getTrailerPath(),
                        preferences, outText);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Platform.runLater(() ->
            {
                if (runWhenFinish != null) {
                    runWhenFinish.run();
                }
                GUICommon.writeToObList("=======================================================", outText);
                GUICommon.writeToObList("FINISH WRITE TO FILE", outText);
                GUICommon.writeToObList("=======================================================", outText);
            });
        });
    }

    public Image getFileIcon() {
        return this.fileIcon;
    }

    public String getFileName() {
        return this.fileNameProperty.get();
    }

    public Boolean getNeedCheck() {
        return this.needCheckProperty.get();
    }

    private void setNeedCheck(Boolean needCheck) {
        this.needCheckProperty.set(needCheck);
    }

    public String getFileExtenion() {
        return this.fileExtenionProperty.get();
    }

    public Boolean hasMovie() {
        return (this.moviePaths.getMovies().length() > 0);
    }

    public Boolean hasNfo() {
        return (this.moviePaths.nfoPath != null);
    }

    public Path getNfoPath() {
        return this.moviePaths.nfoPath;
    }

    public Path getActorPath() {
        return this.moviePaths.getActorFolderPath();
    }

    public long getMovieSize() {
        return this.size;
    }

    public MovieV2 getMovieData() {
        return this.movieData.getOrNull();
    }

    public void setMovieData(MovieV2 movieData) {
        this.movieData = Option.of(movieData);
        MovieCacheInstance.cache().put(this.getDirPath(), movieData);
    }

    @Override
    public Boolean apply() {
        // TODO Auto-generated method stub
        // system.out.println("Thread Running >> ");
        return this.DoCheckFile(this.getNeedCheck());
    }

    @Override
    public void run() {
        this.apply();
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public boolean isDirectory() {
        return this.directory;
    }

    public Path getDirPath() {
        return this.dirPath;
    }

    public MoviePaths getMoviePaths() {
        return this.moviePaths;
    }

    public BooleanProperty getNeedCheckProperty() {
        return this.needCheckProperty;
    }

    public StringProperty getFileNameProperty() {
        return this.fileNameProperty;
    }

    public StringProperty getFileExtenionProperty() {
        return this.fileExtenionProperty;
    }

    class MoviePaths {
        Vector<Tuple2<Path, Long>> movies = Vector.empty();
        Path nfoPath = null;
        Path posterPath = null;
        Path coverPath = null;
        Path folderImagePath = null;
        Path trailerPath = null;
        Path extraArtFolderPath = null;
        Path actorFolderPath = null;

        public MoviePaths() {
        }

        void resolvePaths(Path dirPath, String movName) {
            GeneralSettings preferences = GUICommon.getPreferences();

            this.nfoPath = dirPath.resolve(MovieV2.getFileNameOfNfo(movName, preferences.getNfoNamedMovieDotNfo()));
            this.posterPath = dirPath.resolve(MovieV2.getFileNameOfPoster(movName, preferences.getNoMovieNameInImageFiles()));
            this.coverPath = dirPath.resolve(MovieV2.getFileNameOfFanart(movName, preferences.getNoMovieNameInImageFiles()));
            this.folderImagePath = dirPath.resolve(MovieV2.getFileNameOfFolderJpg());
            this.trailerPath = dirPath.resolve("-trailer.mp4");
            this.actorFolderPath = dirPath.resolve(".actors");
            this.extraArtFolderPath = dirPath.resolve("extrafanart");
        }

        void clear() {
            this.movies = Vector.empty();
            this.nfoPath = null;
        }

        public Vector<Tuple2<Path, Long>> getMovies() {
            return this.movies;
        }

        public Path getNfoPath() {
            return this.nfoPath;
        }

        public Path getPosterPath() {
            return this.posterPath;
        }

        public Path getCoverPath() {
            return this.coverPath;
        }

        public Path getFolderImagePath() {
            return this.folderImagePath;
        }

        public Path getTrailerPath() {
            return this.trailerPath;
        }

        public Path getExtraArtFolderPath() {
            return this.extraArtFolderPath;
        }

        public Path getActorFolderPath() {
            return this.actorFolderPath;
        }

        public void setMovies(Vector<Tuple2<Path, Long>> movies) {
            this.movies = movies;
        }

        public void setNfoPath(Path nfoPath) {
            this.nfoPath = nfoPath;
        }

        public void setPosterPath(Path posterPath) {
            this.posterPath = posterPath;
        }

        public void setCoverPath(Path coverPath) {
            this.coverPath = coverPath;
        }

        public void setFolderImagePath(Path folderImagePath) {
            this.folderImagePath = folderImagePath;
        }

        public void setTrailerPath(Path trailerPath) {
            this.trailerPath = trailerPath;
        }

        public void setExtraArtFolderPath(Path extraArtFolderPath) {
            this.extraArtFolderPath = extraArtFolderPath;
        }

        public void setActorFolderPath(Path actorFolderPath) {
            this.actorFolderPath = actorFolderPath;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof MoviePaths)) return false;
            final MoviePaths other = (MoviePaths) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$movies = this.movies;
            final Object other$movies = other.movies;
            if (this$movies == null ? other$movies != null : !this$movies.equals(other$movies)) return false;
            final Object this$nfoPath = this.nfoPath;
            final Object other$nfoPath = other.nfoPath;
            if (this$nfoPath == null ? other$nfoPath != null : !this$nfoPath.equals(other$nfoPath)) return false;
            final Object this$posterPath = this.posterPath;
            final Object other$posterPath = other.posterPath;
            if (this$posterPath == null ? other$posterPath != null : !this$posterPath.equals(other$posterPath))
                return false;
            final Object this$coverPath = this.coverPath;
            final Object other$coverPath = other.coverPath;
            if (this$coverPath == null ? other$coverPath != null : !this$coverPath.equals(other$coverPath))
                return false;
            final Object this$folderImagePath = this.folderImagePath;
            final Object other$folderImagePath = other.folderImagePath;
            if (this$folderImagePath == null ? other$folderImagePath != null : !this$folderImagePath.equals(other$folderImagePath))
                return false;
            final Object this$trailerPath = this.trailerPath;
            final Object other$trailerPath = other.trailerPath;
            if (this$trailerPath == null ? other$trailerPath != null : !this$trailerPath.equals(other$trailerPath))
                return false;
            final Object this$extraArtFolderPath = this.extraArtFolderPath;
            final Object other$extraArtFolderPath = other.extraArtFolderPath;
            if (this$extraArtFolderPath == null ? other$extraArtFolderPath != null : !this$extraArtFolderPath.equals(other$extraArtFolderPath))
                return false;
            final Object this$actorFolderPath = this.actorFolderPath;
            final Object other$actorFolderPath = other.actorFolderPath;
            if (this$actorFolderPath == null ? other$actorFolderPath != null : !this$actorFolderPath.equals(other$actorFolderPath))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof MoviePaths;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $movies = this.movies;
            result = result * PRIME + ($movies == null ? 43 : $movies.hashCode());
            final Object $nfoPath = this.nfoPath;
            result = result * PRIME + ($nfoPath == null ? 43 : $nfoPath.hashCode());
            final Object $posterPath = this.posterPath;
            result = result * PRIME + ($posterPath == null ? 43 : $posterPath.hashCode());
            final Object $coverPath = this.coverPath;
            result = result * PRIME + ($coverPath == null ? 43 : $coverPath.hashCode());
            final Object $folderImagePath = this.folderImagePath;
            result = result * PRIME + ($folderImagePath == null ? 43 : $folderImagePath.hashCode());
            final Object $trailerPath = this.trailerPath;
            result = result * PRIME + ($trailerPath == null ? 43 : $trailerPath.hashCode());
            final Object $extraArtFolderPath = this.extraArtFolderPath;
            result = result * PRIME + ($extraArtFolderPath == null ? 43 : $extraArtFolderPath.hashCode());
            final Object $actorFolderPath = this.actorFolderPath;
            result = result * PRIME + ($actorFolderPath == null ? 43 : $actorFolderPath.hashCode());
            return result;
        }

        public String toString() {
            return "DirectoryEntry.MoviePaths(movies=" + this.getMovies() + ", nfoPath=" + this.getNfoPath() + ", posterPath=" + this.getPosterPath() + ", coverPath=" + this.getCoverPath() + ", folderImagePath=" + this.getFolderImagePath() + ", trailerPath=" + this.getTrailerPath() + ", extraArtFolderPath=" + this.getExtraArtFolderPath() + ", actorFolderPath=" + this.getActorFolderPath() + ")";
        }
    }

}
