package org.nagoya.model;

import cyclops.control.Try;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.UtilCommon;
import org.nagoya.model.dataitem.ActorV2;
import org.nagoya.model.dataitem.FxThumb;
import org.nagoya.preferences.GeneralSettings;
import org.nagoya.preferences.RenameSettings;
import org.nagoya.system.ExecuteSystem;
import org.nagoya.system.Systems;
import org.nagoya.system.cache.MovieV2Cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class MovieFolder {

    private final DirectoryEntry directoryEntry;

    private Vector<Tuple2<Path, Long>> movieFilesPath;
    private Path nfoPath = null;
    private Path frontCoverPath = null;
    private Path backCoverPath = null;
    private Path folderImagePath = null;
    private Path trailerPath = null;
    private Path extraArtFolderPath = null;
    private Path actorFolderPath = null;

    private Option<DirectoryEntry> dFrontCover = Option.none();
    private Option<DirectoryEntry> dBackCover = Option.none();
    private Option<DirectoryEntry> dFolderImage = Option.none();
    private Option<DirectoryEntry> dActorFolder = Option.none();
    private Option<DirectoryEntry> dExtraImageFolder = Option.none();
    private Option<DirectoryEntry> dNfoFile = Option.none();

    private Option<MovieV2> movieData = Option.none();

    @NotNull
    public static Option<MovieFolder> create(@NotNull DirectoryEntry entry) {
        var scanMovie = getMovieFilePaths(entry.getChildrenEntry());
        if (scanMovie.isEmpty() || scanMovie.length() > 10) {
            return Option.none();
        }

        return Option.of(new MovieFolder(entry, scanMovie));
    }

    public static boolean writeMovie(MovieFolder moviePath, Option<ObservableList<String>> outText) {

        if (moviePath == null || moviePath.getMovieData().isEmpty()) {
            GUICommon.writeToObList(">> Error >> No movie data exist", outText);
            return false;
        }

        MovieV2 movieToSave = moviePath.getMovieData().get();
        Path basePath = moviePath.getDirectoryEntry().getValue();

        if (!movieToSave.hasValidTitle()) {
            GUICommon.writeToObList(">> Error >> No match for this movie in the array or there was no title filled in; skipping writing", outText);
            return false;
        }

        GUICommon.writeToObList("=======================================================", outText);
        GUICommon.writeToObList("Writing movie: " + movieToSave.getMovieTitle().getTitle(), outText);
        GUICommon.writeToObList("=======================================================", outText);

        GeneralSettings preferences = Systems.getPreferences();

        if (preferences.getRenameMovieFile()) {
            String movID = RenameSettings.getSuitableFileName(movieToSave);

            try {
                int Naming = 1;
                for (Path p : moviePath.getMovieFilesPath().map(Tuple2::_1)) {
                    String ext = FilenameUtils.getExtension(p.getFileName().toString());
                    Path idealPath;
                    if (moviePath.getMovieFilesPath().size() > 1) {
                        idealPath = basePath.resolve(movID + " pt" + Naming + "." + ext);
                        Naming++;
                    } else {
                        idealPath = basePath.resolve(movID + "." + ext);
                    }
                    GUICommon.writeToObList(">> " + idealPath.getFileName(), outText);
                    Files.move(p, idealPath);
                }

                if (moviePath.hasNfo()) {
                    Files.deleteIfExists(moviePath.getNfoPath());
                    Files.deleteIfExists(moviePath.getFrontCoverPath());
                    Files.deleteIfExists(moviePath.getBackCoverPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            moviePath.resolvePaths(basePath, movID);
        }

        movieToSave.useLocalPathForFrontCover(moviePath.getFrontCoverPath());

        movieToSave
                .writeNfoFile(
                        moviePath.getNfoPath(),
                        outText)
                .writeCoverToFile(
                        preferences.getWriteFanartAndPostersPreference() ? moviePath.getFrontCoverPath() : null,
                        preferences.getWriteFanartAndPostersPreference() ? moviePath.getBackCoverPath() : null,
                        preferences.getWriteFanartAndPostersPreference() ? moviePath.getFolderImagePath() : null,
                        outText)
                .writeExtraImages(
                        preferences.getExtraFanartScrapingEnabledPreference() ? moviePath.getExtraArtFolderPath() : null,
                        outText)
                .writeActorImages(
                        moviePath.getActorFolderPath(),
                        outText)
        ;

        GUICommon.writeToObList("=======================================================", outText);
        GUICommon.writeToObList("FINISH WRITE TO FILE", outText);
        GUICommon.writeToObList("=======================================================", outText);

        return true;
    }

    private MovieFolder(DirectoryEntry entry, Vector<Tuple2<Path, Long>> moviePaths) {
        this.directoryEntry = entry;
        this.movieFilesPath = moviePaths;

        String movieName = MovieV2.getUnstackedMovieName(this.movieFilesPath.get()._1);

        this.resolvePaths(this.directoryEntry.getValue(), movieName);

        // Temp fix
    /*    Path oldExtraPath = this.getDirectoryEntry().getValue().resolve("extrafanart");
        if (Files.exists(oldExtraPath) && Files.isDirectory(oldExtraPath)) {
            UtilCommon.renameFile(oldExtraPath, this.extraArtFolderPath);
            this.directoryEntry.resetChild();
        }
        Path oldBackCoverPath = this.getDirectoryEntry().getValue().resolve(movieName + "-landscape.jpg");
        if (Files.exists(oldBackCoverPath)) {
            UtilCommon.renameFile(oldBackCoverPath, this.backCoverPath);
            this.directoryEntry.resetChild();
        }*/

        this.mapPathsToLocalChild(this.directoryEntry.getChildrenEntry());
    }

    private void resolvePaths(@NotNull Path dirPath, String movName) {
        this.nfoPath = dirPath.resolve(RenameSettings.getFileNameNfo(movName));
        this.frontCoverPath = dirPath.resolve(RenameSettings.getFileNameFrontCover(movName));
        this.backCoverPath = dirPath.resolve(RenameSettings.getFileNameBackCover(movName));
        this.folderImagePath = dirPath.resolve(RenameSettings.getFileNameFolderJpg());
        this.trailerPath = dirPath.resolve(RenameSettings.getFileNameTrailer(movName));
        this.actorFolderPath = dirPath.resolve(RenameSettings.getFolderNameActors());
        this.extraArtFolderPath = dirPath.resolve(RenameSettings.getFolderNameExtraImage());
    }

    private void mapPathsToLocalChild(Vector<DirectoryEntry> entries) {
        this.dNfoFile = pathToEntry(entries, this.nfoPath);

        if (this.dNfoFile.isEmpty()) {
            this.dNfoFile = entries.find(entry -> UtilCommon.checkFileExt.test(entry.getValue(), ".nfo"));
        }

        this.dFrontCover = pathToEntry(entries, this.frontCoverPath);
        this.dBackCover = pathToEntry(entries, this.backCoverPath);
        this.dFolderImage = pathToEntry(entries, this.folderImagePath);
        this.dExtraImageFolder = pathToEntry(entries, this.extraArtFolderPath);
        this.dActorFolder = pathToEntry(entries, this.actorFolderPath);

        //movieData = MovieV2Cache.getInstance().loadFromCache(dNfoFile)

        if (this.hasNfo()) {
            Path nfoPath = this.dNfoFile.get().getValue();

            //GUICommon.debugMessage(nfoPath.toString());

            this.movieData = MovieV2Cache.getInstance().loadFromCache(nfoPath);

            if (this.movieData.isEmpty()) {
                this.movieData = Option.of(this)
                        .flatMap(mp -> mp.tryLoadMovie().map(v -> Tuple.of(v, mp)))
                        .map(MovieFolder::patchFrontCoverWithLocal)
                        .map(MovieFolder::patchBackCoverWithLocal)
                        .map(MovieFolder::loadLocalExtraImage)
                        .map(MovieFolder::loadLocalActorImage)
                        .map(t -> t._1);

                MovieV2Cache.getInstance().putCache(nfoPath, this.movieData.getOrNull(),
                        io.vavr.control.Try.of(() -> Files.getLastModifiedTime(nfoPath)).getOrNull());
            }
        }
    }

    public Option<MovieV2> getMovieData() {
        return this.movieData;
    }

    public void setMovieData(Option<MovieV2> movieData) {
        this.movieData = movieData;
    }

    public DirectoryEntry getDirectoryEntry() {
        return this.directoryEntry;
    }

    void clear() {
        this.movieFilesPath = Vector.empty();
        if (this.hasNfo()) {
            MovieV2Cache.getInstance().removeCache(this.nfoPath);
            this.nfoPath = null;
        }
        this.dFrontCover = Option.none();
        this.dBackCover = Option.none();
        this.dFolderImage = Option.none();
        this.dActorFolder = Option.none();
        this.dExtraImageFolder = Option.none();
        this.dNfoFile = Option.none();
    }

    void releaseMemory() {
        this.movieData.peek(MovieV2::releaseMemory);
    }

    public Vector<Tuple2<Path, Long>> getMovieFilesPath() {
        return this.movieFilesPath;
    }

    public boolean hasNfo() {
        return this.dNfoFile.isDefined();
    }

    public Path getNfoPath() {
        return this.nfoPath;
    }

    public Path getFrontCoverPath() {
        return this.frontCoverPath;
    }

    public Path getBackCoverPath() {
        return this.backCoverPath;
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

    public Option<DirectoryEntry> getdFrontCover() {
        return this.dFrontCover;
    }

    public Option<DirectoryEntry> getdBackCover() {
        return this.dBackCover;
    }

    public Option<DirectoryEntry> getdFolderImage() {
        return this.dFolderImage;
    }

    public Option<DirectoryEntry> getdActorFolder() {
        return this.dActorFolder;
    }

    public Option<DirectoryEntry> getdExtraImageFolder() {
        return this.dExtraImageFolder;
    }

    public Option<DirectoryEntry> getdNfoFile() {
        return this.dNfoFile;
    }

    public void setdNfoFile(Option<DirectoryEntry> dNfoFile) {
        this.dNfoFile = dNfoFile;
    }

    public Option<MovieV2> tryLoadMovie() {


        return MovieV2.fromNfoFile(this.getNfoPath()).onEmpty(() -> this.setdNfoFile(Option.none()));
    }

    @Override
    public String toString() {
        return "MovieFolder{" +
                "movieFilesPath=" + this.movieFilesPath +
                ", nfoPath=" + this.nfoPath +
                ", frontCoverPath=" + this.frontCoverPath +
                ", backCoverPath=" + this.backCoverPath +
                ", folderImagePath=" + this.folderImagePath +
                ", trailerPath=" + this.trailerPath +
                ", extraArtFolderPath=" + this.extraArtFolderPath +
                ", actorFolderPath=" + this.actorFolderPath +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MovieFolder that = (MovieFolder) o;
        return this.movieFilesPath.equals(that.movieFilesPath) &&
                Objects.equals(this.nfoPath, that.nfoPath) &&
                Objects.equals(this.frontCoverPath, that.frontCoverPath) &&
                Objects.equals(this.backCoverPath, that.backCoverPath) &&
                Objects.equals(this.folderImagePath, that.folderImagePath) &&
                Objects.equals(this.trailerPath, that.trailerPath) &&
                Objects.equals(this.extraArtFolderPath, that.extraArtFolderPath) &&
                Objects.equals(this.actorFolderPath, that.actorFolderPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.movieFilesPath, this.nfoPath, this.frontCoverPath, this.backCoverPath, this.folderImagePath, this.trailerPath, this.extraArtFolderPath, this.actorFolderPath);
    }

    private static Option<DirectoryEntry> pathToEntry(Vector<DirectoryEntry> entries, Path path) {
        if (entries == null || path == null) {
            return Option.none();
        }
        return entries.find(c -> c.getValue().equals(path));
    }

    private static Vector<Tuple2<Path, Long>> getMovieFilePaths(@NotNull Vector<DirectoryEntry> pathVector) {
        Vector<String> movExt = Vector.of(MovieFilenameFilter.acceptedMovieExtensions);

        return pathVector
                .map(TreeItem::getValue)
                .filter(path -> movExt.find(ext -> UtilCommon.checkFileExt.test(path, ext)).isDefined())
                //.peek(p->GUICommon.debugMessage(p.getFileNameProperty().toString()))
                .map(path -> Tuple.of(path, (Try.withCatch(() -> Files.size(path) / 1024 / 1024, IOException.class).orElse(0L))));
    }

    @Contract("_ -> param1")
    private static Tuple2<MovieV2, MovieFolder> patchFrontCoverWithLocal(@NotNull Tuple2<MovieV2, MovieFolder> tuple2) {
        Path poster = tuple2._1.getImgFrontCover()
                .filter(FxThumb::isLocal)
                .map(FxThumb::getLocalPath)
                .getOrElse(tuple2._2.frontCoverPath);

        if (Files.exists(poster)) {
            tuple2._1.setImgFrontCover(FxThumb.of(poster));
        } else {
            tuple2._1.setImgFrontCover(Option.none());
        }
        return tuple2;
    }

    @Contract("_ -> param1")
    private static Tuple2<MovieV2, MovieFolder> patchBackCoverWithLocal(@NotNull Tuple2<MovieV2, MovieFolder> tuple2) {
        if (Files.exists(tuple2._2.backCoverPath)) {
            //GUICommon.debugMessage("patchBackCoverWithLocal Files.exists");
            tuple2._1.getImgBackCover().peek((t) -> t.setLocalPath(tuple2._2.backCoverPath));
        }
        return tuple2;
    }

    @Contract("_ -> param1")
    private static Tuple2<MovieV2, MovieFolder> loadLocalExtraImage(@NotNull Tuple2<MovieV2, MovieFolder> tuple2) {
        tuple2._2.getdExtraImageFolder()
                .map(MovieFolder::tryLoadExtraImage)
                .peek(s -> s.onSuccess(tuple2._1::setImgExtras));

        return tuple2;
    }

    @Contract("_ -> param1")
    private static Tuple2<MovieV2, MovieFolder> loadLocalActorImage(@NotNull Tuple2<MovieV2, MovieFolder> tuple2) {
        tuple2._2.getdActorFolder()
                .peek(d -> {
                    Vector<ActorV2> a = tryLoadLocalActorImage(d, tuple2._1.getActorListBlock());
                    tuple2._1.setActorList(FXCollections.observableList(a.asJava()));
                });

        return tuple2;
    }

    @NotNull
    private static Future<Stream<FxThumb>> tryLoadExtraImage(DirectoryEntry extraImagePath) {

        return Future.of(Systems.getExecutorServices(ExecuteSystem.role.IMAGE),
                () -> extraImagePath.getChildrenEntry().toStream()
                        .filter(DirectoryEntry::isFile)
                        .map(TreeItem::getValue)
                        .filter(path -> UtilCommon.checkFileExt.test(path, ".jpg"))
                        .map(FxThumb::of)
        );
    }

    private static Vector<ActorV2> tryLoadLocalActorImage(@NotNull DirectoryEntry actorImagePath, List<ActorV2> actorList) {

        Vector<ActorV2> actorV2s = Vector.ofAll(actorList);

        actorImagePath.getChildrenEntry().filter(DirectoryEntry::isFile).map(TreeItem::getValue).forEach(path ->
                        actorV2s.find(a -> path.getFileName().toString().startsWith(a.getName().replace(' ', '_')))
                                .peek(a -> a.setLocalImage(FxThumb.of(path)))
                //.peek(a -> GUICommon.debugMessage(() -> "Actor Image Found on Local"))
        );

        return actorV2s;
    }
}

