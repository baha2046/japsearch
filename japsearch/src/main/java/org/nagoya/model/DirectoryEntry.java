package org.nagoya.model;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vavr.CheckedFunction0;
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
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.model.dataitem.ActorV2;
import org.nagoya.model.dataitem.FxThumb;
import org.nagoya.system.ExecuteSystem;
import org.nagoya.system.Systems;
import org.nagoya.system.cache.IconCache;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.List;


public class DirectoryEntry extends TreeItem<Path> implements CheckedFunction0<Boolean>, Runnable {

    private DirectoryEntry parent = null;
    private Vector<DirectoryEntry> childList = Vector.empty();
    private boolean bFirstRun = true;
    private final boolean directory;
    private FileTime lastModified = null;

    private Option<MovieFolder> movieFolder = Option.none();
    private Option<GalleryFolder> galleryFolder = Option.none();
    //private Option<MovieV2> movieData = Option.none();

    private boolean needCheck = true;
    private final BooleanProperty needCheckProperty = new SimpleBooleanProperty(true);
    private final StringProperty fileNameProperty = new SimpleStringProperty("");
    private final StringProperty fileExtensionProperty = new SimpleStringProperty("");

    private Option<Image> fileIcon;
    private Option<Long> size = Option.none();

    private int lastSelectedIndex = -1;

    private DirectoryEntry() {
        this.directory = false;
    }

    private DirectoryEntry(@NotNull Path inPath, DirectoryEntry parent) {

        this.setEntryParent(parent);
        this.setValue(inPath);

        this.needCheckProperty.setValue(true);
        this.fileNameProperty.set(Option.of(inPath.getFileName()).map(Path::toString).getOrElse(""));

        this.directory = Files.isDirectory(inPath);


        if (!this.isDirectory()) {
            this.fileNameProperty.set(FilenameUtils.removeExtension(this.fileNameProperty.get()));
            this.fileExtensionProperty.set("File");
            this.size = io.vavr.control.Try.of(() -> Files.size(this.getValue()) / 1024 / 1024).toOption();
            if (this.size.getOrElse(0L) < 1L) {
                this.size = Option.none();
            }

        } else {
            this.fileExtensionProperty.set("Folder");
            this.size = Option.none();
        }

        this.fileIcon = io.vavr.control.Try.of(() -> (ImageIcon) IconCache.getIconFromCache(inPath.toFile()))
                .map(ic -> {
                    BufferedImage bImg;
                    if (ic.getImage() instanceof BufferedImage) {
                        bImg = (BufferedImage) ic.getImage();
                    } else {
                        bImg = new BufferedImage(ic.getImage().getWidth(null), ic.getImage().getHeight(null), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D graphics = bImg.createGraphics();
                        graphics.drawImage(ic.getImage(), 0, 0, null);
                        graphics.dispose();
                    }
                    return bImg;
                })
                .map(bImg -> (Image) SwingFXUtils.toFXImage(bImg, null))
                .toOption();

        this.setGraphic(new ImageView(this.fileIcon.getOrNull()));

    }
    // standard constructors/getters

    public void setEntryParent(DirectoryEntry parent) {
        this.parent = parent;
    }

    public DirectoryEntry getEntryParent() {
        return this.parent;
    }

    public Vector<DirectoryEntry> getChildrenEntry() {
        //GUICommon.debugMessage("getChildrenEntry " + this.getValue().getFileName().toString());

        if (this.bFirstRun) {
            this.bFirstRun = false;
            this.childList = this.buildChildren();
        }

        return this.childList;
    }

    public void setChildEntry(Vector<DirectoryEntry> entries) {
        this.childList = entries;
    }

    public void removeChild(DirectoryEntry entry) {
        this.setChildEntry(this.getChildrenEntry().remove(entry));
    }

    private Vector<DirectoryEntry> buildChildren() {
        Path path = this.getValue();

        //GUICommon.debugMessage("buildChildren " + path.toString());
        if (path != null && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {

            //if (!path.getFileName().toString().contains("Trash")) {

            return io.vavr.control.Try.withResources(() -> Files.newDirectoryStream(path))
                    .of(ds -> Vector.ofAll(ds)
                            .sorted(PATH_COMPARATOR)
                            .map(p -> DirectoryEntry.of(p, this)))
                    .onFailure(Throwable::printStackTrace)
                    .getOrElse(Vector.empty());
            //}
        }
        return Vector.empty();
    }

    public void releaseMemory() {
        this.movieFolder.peek(MovieFolder::releaseMemory);
    }

    public void releaseChildMemory() {
        this.getChildrenEntry().forEach(DirectoryEntry::releaseMemory);
    }

    private static final Comparator<Path> PATH_COMPARATOR = (p, q) -> {
        boolean pd = Files.isDirectory(p);
        boolean qd = Files.isDirectory(q);

        if (pd && !qd) {
            return -1;
        } else if (!pd && qd) {
            return 1;
        } else {
            return p.getFileName().toString().compareToIgnoreCase(q.getFileName().toString());
        }
    };

    public void resetChild() {
        this.childList = null;
        this.bFirstRun = true;
    }

    @NotNull
    @Contract("_ -> new")
    public static DirectoryEntry of(Path inPath) {
        return new DirectoryEntry(inPath, null);
    }

    @Contract("_, _ -> new")
    @NotNull
    public static DirectoryEntry of(Path inPath, DirectoryEntry parent) {
        return new DirectoryEntry(inPath, parent);
    }

    public static DirectoryEntry getAndExe(Path inPath) {
        DirectoryEntry directoryEntry = new DirectoryEntry(inPath, null);
        Systems.useExecutors(directoryEntry);
        return directoryEntry;
    }

    @NotNull
    @Contract(pure = true)
    public static Callback<DirectoryEntry, Observable[]> extractor() {
        return (DirectoryEntry p) -> new Observable[]{p.getNeedCheckProperty()};
    }

    private Future<Void> tryLoadLocalActorImageAysc(DirectoryEntry actorImagePath, List<ActorV2> actorList) {

        Stream<ActorV2> actorV2Stream = Stream.ofAll(actorList);

        return Future.run(Systems.getExecutorServices(ExecuteSystem.role.IMAGE),
                () -> actorImagePath.getChildrenEntry().toStream()
                        .filter(DirectoryEntry::isFile)
                        .map(TreeItem::getValue)
                        //.peek(p -> GUICommon.debugMessage("--Actor " + p.getFileName().toString()))
                        .peek(path -> {
                            actorV2Stream
                                    .find(a -> path.getFileName().toString().startsWith(a.getName().replace(' ', '_')))
                                    .peek(a -> a.setLocalImage(FxThumb.of(path)))
                                    .peek(a -> GUICommon.debugMessage("Actor Image Found on Local"))
                            ;
                        })
        );
    }

    private void extraCheckForFile() {
        String strExtension = null;
        try {
            this.lastModified = Files.getLastModifiedTime(this.getValue());
            strExtension = Files.probeContentType(this.getValue());

            if (strExtension == null) {
                InputStream is = new BufferedInputStream(new FileInputStream(this.getValue().toFile()));
                strExtension = URLConnection.guessContentTypeFromStream(is);
                is.close();
            }
            String ext = strExtension;
            Platform.runLater(() -> this.fileExtensionProperty.set(ext));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void extraCheckForDirectory() {

        // Check if there has movie files and then build paths data
        this.movieFolder = MovieFolder.create(this);

        this.size = this.movieFolder.map(v -> v.getMovieFilesPath().map(Tuple2::_2).sum().longValue());
        if (this.size.getOrElse(0L) < 1L) {
            this.size = Option.none();
        }

        if (this.movieFolder.isEmpty()) {
            this.galleryFolder = GalleryFolder.create(this);
        }
    }

    public void clearCache() {
        this.movieFolder.peek(MovieFolder::clear);
        this.movieFolder = Option.none();
        this.resetChild();
        this.setNeedCheck(true);
        this.setNeedCheckProperty(true);
    }

    public void writeMovieDataToFile(Option<ObservableList<String>> outText, Runnable runWhenFinish) {

        if (this.movieFolder.isEmpty()) {
            GUICommon.writeToObList(">> Error >> No movie files exist at this directory!", outText);
            return;
        }

        Single.fromCallable(() -> MovieFolder.writeMovie(this.movieFolder.get(), outText))
                .doOnSuccess(b -> {
                    Platform.runLater(() ->
                    {
                        Systems.getDirectorySystem().reloadFile(this);
                        //this.clearCache();
                        //this.apply();
                        if (runWhenFinish != null) {
                            runWhenFinish.run();
                        }
                    });
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }


    public Image getFileIcon() {
        return this.fileIcon.getOrNull();
    }

    public String getFileName() {
        return this.fileNameProperty.get();
    }

    public Boolean getNeedCheck() {
        return this.needCheck;
    }

    private void setNeedCheck(boolean needCheck) {
        this.needCheck = needCheck;
    }

    public String getFileExtenion() {
        return this.fileExtensionProperty.get();
    }

    public Boolean hasMovie() {
        return this.movieFolder.map(v -> v.getMovieFilesPath().length() > 0).getOrElse(false);
    }

    public Boolean hasNfo() {
        return this.movieFolder.map(MovieFolder::hasNfo).getOrElse(false);
    }

    public boolean isGalleryFolder() {
        return this.galleryFolder.isDefined();
    }

    public Stream<FxThumb> getGalleryImages() {
        return this.galleryFolder.map(v -> v.getGalleryImages().toStream()).getOrElse(Stream.empty());
    }

    public Option<Long> getMovieSize() {
        return this.size;
    }

    public MovieV2 getMovieData() {
        return this.movieFolder.flatMap(MovieFolder::getMovieData).getOrNull();
    }

    public void setMovieData(MovieV2 movieData) {
        this.movieFolder.peek(v -> v.setMovieData(Option.of(movieData)));
        //MovieCacheInstance.cache().put(this.getDirPath(), movieData);
    }

    @Override
    public Boolean apply() {
        //return this.DoCheckFile(this.getNeedCheck());

        if (this.getNeedCheck()) {
            //Benchmark b = new Benchmark(false, "DirectorEntry - " + this.getValue().getFileName().toString());
            // system.out.println("CHECKING~~~~~~~~~~~~~~~~ ");

            if (this.isFile()) {
                this.extraCheckForFile();
            } else {
                //GUICommon.debugMessage(this.getValue().toString());
                this.extraCheckForDirectory();
            }
            //b.B("F ");
            this.setNeedCheck(false);
            Platform.runLater(() -> this.setNeedCheckProperty(false));

            // GUICommon.debugMessage(this.getValue().getFileName().toString() + " || " + this.movieData.map(MovieV2::getMovieTitle).map(Title::getTitle).getOrElse(""));
        }
        return true;
    }

    @Override
    public void run() {
        this.apply();
    }

    public Path getFilePath() {
        return this.getValue();
    }

    public boolean isDirectory() {
        return this.directory;
    }

    public boolean isFile() {
        return !this.directory;
    }

    public Path getDirPath() {
        return this.isDirectory() ? this.getValue() : this.getValue().getParent();
    }

    public Option<MovieFolder> getMovieFolder() {
        return this.movieFolder;
    }

    public BooleanProperty getNeedCheckProperty() {
        return this.needCheckProperty;
    }

    public void setNeedCheckProperty(boolean b) {
        this.needCheckProperty.set(b);
    }

    public StringProperty getFileNameProperty() {
        return this.fileNameProperty;
    }

    public StringProperty getFileExtensionProperty() {
        return this.fileExtensionProperty;
    }

    public int getLastSelectedIndex() {
        return this.lastSelectedIndex;
    }

    public void setLastSelectedIndex(int lastSelectedIndex) {
        this.lastSelectedIndex = lastSelectedIndex;
    }

    public FileTime getLastModified() {
        return this.lastModified;
    }
}
