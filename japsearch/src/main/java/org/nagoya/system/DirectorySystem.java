package org.nagoya.system;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.UtilCommon;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.preferences.GuiSettings;
import org.nagoya.system.event.CustomEvent;
import org.nagoya.system.event.CustomEventSource;
import org.nagoya.system.event.CustomEventType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DirectorySystem implements CustomEventSource {
    private static final Comparator<Path> COMP_FOR_PATH = (file1, file2) -> {
        // Directory before non-directory
        if (Files.isDirectory(file1) && !Files.isDirectory(file2)) {
            return -1;
        }
        // Non-directory after directory
        else if (!Files.isDirectory(file1) && Files.isDirectory(file2)) {
            return 1;
        }
        // Alphabetic order otherwise
        else {
            return file1.compareTo(file2);
        }
    };

    private static final Comparator<DirectoryEntry> COMP_FOR_DIR = (d1, d2) -> {
        // Movie before others
        if (d1.hasNfo() && !d2.hasNfo()) {
            return -1;
        }
        // Non-movie after movie
        else if (!d1.hasNfo() && d2.hasNfo()) {
            return 1;
        }
        // Both Movie - Release Date order
        else if (d1.hasNfo() && d2.hasNfo()) {
            if (d1.getMovieData().getReleaseDates().isEmpty() && d2.getMovieData().getReleaseDates().isDefined()) {
                return -1;
            } else if (d1.getMovieData().getReleaseDates().isDefined() && d2.getMovieData().getReleaseDates().isEmpty()) {
                return 1;
            } else if (d1.getMovieData().getReleaseDates().isDefined() && d2.getMovieData().getReleaseDates().isDefined()) {
                return d1.getMovieData().getReleaseDates().get().getReleaseDate().compareTo(d2.getMovieData().getReleaseDates().get().getReleaseDate());
            }
        }

        // Both not Movie or no release date
        return COMP_FOR_PATH.compare(d1.getFilePath(), d2.getFilePath());

    };

    public final DoubleProperty currentScrollbarPos;

    private final Map<String, Double> scrollPosMap;

    private DirectoryEntry currentRootEntry;
    private final DirectoryEntry movieRootEntry;

    private LiveDirs<String, Path> liveDirs;

    //public TreeItem<Path> rootTree;


    public DirectorySystem() {

        this.currentScrollbarPos = new SimpleDoubleProperty(0);
        this.scrollPosMap = new LinkedHashMap<>();

        Path path = GuiSettings.getInstance().getDirectory(GuiSettings.Key.avDirectory);
        // directoryTreeManager = new DirectoryTreeManager(path, Path.of("avDir.txt"));

        this.movieRootEntry = DirectoryEntry.of(GUICommon.getGuiSettings().getDirectory(GuiSettings.Key.avDirectory));

        this.currentRootEntry = this.movieRootEntry;
        this.doDirectoryChange(null, -1);

        this.buildDirectoryTree();

        //this.initLiveDir();
        // this.ioThread.start();

        //this.currentDirectoryStringProperty = new SimpleStringProperty();
        //this.currentDirectoryStringProperty.addListener((ChangeListener<? super String>) (observable, oldValue, newValue) -> runnable.accept(this.directoryEntries));
    }

    public DirectoryEntry getMovieRootEntry() {
        return this.movieRootEntry;
    }

    public void shutdown() {
    }

    public static Vector<Path> readPath(Path path) {

        return Try.withResources(() -> Files.newDirectoryStream(path))
                .of(Vector::ofAll)
                .onFailure(Throwable::printStackTrace)
                .getOrElse(Vector.empty());
    }

    public static List<DirectoryEntry> loadPath(@NotNull Path path) {
        return readPath(path)/*.sorted(COMP_FOR_PATH)*/.map(DirectoryEntry::of).asJava();
    }

    private void initLiveDir() {
        Path path = GuiSettings.getInstance().getDirectory(GuiSettings.Key.avDirectory);

        String EXTERNAL = "EXTERNAL";
        this.liveDirs = LiveDirs.getInstance(EXTERNAL);

        this.liveDirs.addTopLevelDirectory(path);

        // handle external changes
        this.liveDirs.model().modifications().subscribe(m -> {
            if (m.getInitiator().equals(EXTERNAL)) {
                // handle external modification, e.g. reload the modified file
                //           reload(m.getPath());
            } else {
                // modification done by this application, no extra action needed
            }
        });

        //this.rootTree = this.liveDirs.getRootElements().get(0);

        // use LiveDirs as a TreeView model
        //    TreeView<Path> treeView = new TreeView<>(this.liveDirs.model().getRoot());
        //   treeView.setShowRoot(false);
        //   treeView.setCellFactory((TreeView<Path> l) -> new PathListTreeCell());
    }

    public void sortContain() {
        this.currentRootEntry.getChildrenEntry().sortBy(COMP_FOR_DIR, i -> i);
    }

    public void changePathTo(Path newPath, Consumer<Path> run, int pos) {

        this.currentRootEntry.releaseChildMemory();
        this.currentRootEntry = this.getRelEntry(newPath).getOrElse(DirectoryEntry.of(newPath));

        this.doDirectoryChange(run, pos);
    }

    public void changePathTo(DirectoryEntry directoryEntry, Consumer<Path> run) {

        this.currentRootEntry.releaseChildMemory();
        this.currentRootEntry = directoryEntry;

        this.doDirectoryChange(run, this.currentRootEntry.getLastSelectedIndex());
    }

    public Option<DirectoryEntry> getRelEntry(Path targetPath) {
        if (targetPath != null && targetPath.startsWith(this.movieRootEntry.getValue())) {
            Path rel = this.movieRootEntry.getValue().relativize(targetPath);

            Option<DirectoryEntry> directoryEntry = Option.of(this.movieRootEntry);
            for (int x = 0; x < rel.getNameCount(); x++) {
                Path check = rel.getName(x);
                Vector<DirectoryEntry> v = directoryEntry.map(DirectoryEntry::getChildrenEntry).getOrElse(Vector.empty());
                directoryEntry = v.find(d -> d.getValue().getFileName().equals(check));
            }
            GUICommon.debugMessage("getRelEntry " + directoryEntry.toString());

            return directoryEntry;
        }

        return Option.none();
    }

    public void buildDirectoryTree() {
        Systems.useExecutors(ExecuteSystem.role.MOVIE, () ->
                this.preloadTree(this.movieRootEntry, 0));
    }

    private void preloadTree(@NotNull DirectoryEntry d, int level) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        Vector<DirectoryEntry> v = d.getChildrenEntry();

        if (level >= 2 || v.isEmpty()) {
            d.apply();
        } else {
            v.forEach(e -> this.preloadTree(e, level + 1));
        }
    }

    public void changePathToBK(Path newPath, Consumer<Path> run, boolean reload) {

        /*if (this.directoryTreeManager.isReady()) {
            Option<TreeItem<Path>> newTree = this.directoryTreeManager.getItem(newPath);
            if (newTree.isDefined()) {
                GUICommon.debugMessage(() -> "DirectorySystem >> DirectoryTreeManager >> Load " + newTree.get().getValue().toString());
            }
        }*/

        Systems.usePriorityExecutors(() -> {

            if (newPath != null && Files.isDirectory(newPath)) {

                GUICommon.debugMessage(() -> "DirectorySystem -- Change to Directory : " + newPath.toString());

                /*if (reload) {
                    DirectoryCacheInstance.cache().evict(newPath);
                } else if (this.currentPath != null) {
                    DirectoryCacheInstance.cache().put(this.currentPath, new ArrayList<>(this.directoryEntries));
                    this.scrollPosMap.put(this.currentPath.toString(), this.currentScrollbarPos.get());
                }*/

                this.currentRootEntry = DirectoryEntry.of(newPath);

                GUICommon.debugMessage(() -> "DirectoryEntry directoryEntry = DirectoryEntry.of");

                // List<DirectoryEntry> directoryEntries = directoryEntry.getChildrenEntry();
                // DirectoryCacheInstance.cache().get(newPath);

                Platform.runLater(() -> {
                    GUICommon.debugMessage(() -> "Show Directory Change");
                    //this.directoryEntries.clear();

                    //this.directoryEntries.setAll(this.currentRootEntry.getChildrenEntry());//.addAll(directoryEntries);

                    if (run != null) {
                        run.accept(newPath);
                    }

                    Future<Seq<Boolean>> seqFuture = Future.sequence(this.currentRootEntry.getChildrenEntry().map(d -> Future.of(Systems.getExecutorServices(ExecuteSystem.role.MOVIE), d)));
                    seqFuture.onComplete((b) -> {
                        Platform.runLater(this::sortContain);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // this.currentScrollbarPos.set(this.scrollPosMap.getOrDefault(this.currentPath.toString(), 0.0));
                    });
                });
            }
        });
    }


    private void doDirectoryChange(Consumer<Path> run, int posToSend) {
        GUICommon.debugMessage(() -> ">>> Show Directory Change");
        //this.directoryEntries.clear();
        //this.directoryEntries.setAll(this.currentRootEntry.getChildrenEntry());

        this.sendFullUpdate(posToSend);

        /*if (run != null) {
            run.accept(this.getCurrentPath());
        }*/

        Flowable.fromIterable(this.currentRootEntry.getChildrenEntry())
                .parallel()
                .runOn(Schedulers.computation())
                .map(DirectoryEntry::apply)
                .sequential()
                .doOnComplete(() -> {
                    if (run != null) {
                        run.accept(this.getCurrentPath());
                    }
                })
                .subscribe();

/*
        Future<Seq<Boolean>> seqFuture = Future.sequence(this.currentRootEntry.getChildrenEntry().map(d -> Future.of(Systems.getExecutorServices(ExecuteSystem.role.IO), d)));
        seqFuture.onComplete((b) -> {
            GUICommon.debugMessage("doDirectoryChange complete");*/
        //Platform.runLater(this::sortContain);
            /*try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
             this.currentScrollbarPos.set(this.scrollPosMap.getOrDefault(this.currentPath.toString(), 0.0));
        */
        //});
    }

    public void upParent(int index, Consumer<Path> run) {
        this.currentRootEntry.setLastSelectedIndex(index);

        if (this.currentRootEntry.getEntryParent() != null) {
            this.changePathTo(this.currentRootEntry.getEntryParent(), run);
        } else {
            if (this.getCurrentPath().getParent() != null) {
                this.changePathTo(this.getCurrentPath().getParent(), run, -1);
            } else if (this.getCurrentPath() != this.getCurrentPath().getRoot() && this.getCurrentPath().getRoot() != null) {
                this.changePathTo(this.getCurrentPath().getRoot(), run, -1);
            }
        }
    }

    public void enterChildIndex(int index, Consumer<Path> run) {

        this.currentRootEntry.setLastSelectedIndex(index);
        this.currentRootEntry.releaseChildMemory();

        DirectoryEntry directoryEntry = this.currentRootEntry.getChildrenEntry().get(index);

        if (directoryEntry.getValue().equals(this.movieRootEntry.getValue())) {
            this.changePathTo(this.movieRootEntry, run);
        } else {
            this.changePathTo(directoryEntry, run);
        }
    }

    public void renameFile(@NotNull DirectoryEntry oldEntry, @NotNull Path newPath) {
        if (UtilCommon.renameFile(oldEntry.getValue(), newPath)) {

            DirectoryEntry newEntry = DirectoryEntry.of(newPath, this.currentRootEntry);
            oldEntry.clearCache();
            this.currentRootEntry.setChildEntry(this.currentRootEntry.getChildrenEntry().replace(oldEntry, newEntry));
            this.sendModify(oldEntry, newEntry);

            Systems.useExecutors(ExecuteSystem.role.IO, newEntry);
        }
    }

    public void reloadFile(@NotNull DirectoryEntry entry) {
        entry.clearCache();
        this.sendModify(entry, entry);

        Systems.useExecutors(ExecuteSystem.role.IO, entry);
    }

    public void deleteFile(int index) {
        this.currentRootEntry.setLastSelectedIndex(-1);
        DirectoryEntry delEntry = this.currentRootEntry.getChildrenEntry().get(index);

        UtilCommon.delFile(delEntry.getValue(), () -> {
            delEntry.clearCache();
            this.currentRootEntry.removeChild(delEntry);
            this.sendDelete(delEntry);
        });


    }

    public void removeEntry(@NotNull DirectoryEntry delEntry) {

        delEntry.resetChild();

        if (this.getCurrentPath().equals(delEntry.getValue().getParent())) {
            this.currentRootEntry.removeChild(delEntry);
            this.sendDelete(delEntry);
        } else {
            if (delEntry.getEntryParent() != null) {
                delEntry.getEntryParent().removeChild(delEntry);
            }
        }
    }

    public void reloadDirectory(int index) {
        this.currentRootEntry.resetChild();
        this.doDirectoryChange(null, index);
    }

    public Vector<DirectoryEntry> getDirectoryEntries() {
        return this.currentRootEntry.getChildrenEntry();
    }

    public Path getCurrentPath() {
        return this.currentRootEntry.getValue();
    }

    private void sendFullUpdate(int posToSend) {
        DirectoryChangeEvent changeEvent = new DirectoryChangeEvent(UpdateType.FULL, this.currentRootEntry.getChildrenEntry(), posToSend);
        this.fireEvent(EVENT_DIRECTORY_TREE_CHANGE, changeEvent);
    }

    private void sendModify(DirectoryEntry oldEntry, DirectoryEntry newEntry) {
        Vector<DirectoryEntry> out = Vector.of(oldEntry, newEntry);
        DirectoryChangeEvent changeEvent = new DirectoryChangeEvent(UpdateType.MODIFICATION, out, -1);
        this.fireEvent(EVENT_DIRECTORY_TREE_CHANGE, changeEvent);
    }

    private void sendDelete(DirectoryEntry delEntry) {
        DirectoryChangeEvent changeEvent = new DirectoryChangeEvent(UpdateType.DELETION, Vector.of(delEntry), -1);
        this.fireEvent(EVENT_DIRECTORY_TREE_CHANGE, changeEvent);
    }

    public static final CustomEventType EVENT_DIRECTORY_TREE_CHANGE = new CustomEventType("EVENT_DIRECTORY_TREE_CHANGE");

    @Override
    public void fireEvent(CustomEventType eventType, Object object) {
        //GUICommon.debugMessage(() -> "fireEvent " + eventType.getName());
        Systems.getEventDispatcher().submit(new CustomEvent(object, eventType));
    }

    public void requestFullSync(@NotNull Consumer<DirectoryChangeEvent> consumer) {
        DirectoryChangeEvent changeEvent = new DirectoryChangeEvent(UpdateType.FULL, this.currentRootEntry.getChildrenEntry(), -1);
        consumer.accept(changeEvent);
    }

    public enum UpdateType {
        FULL,
        DELETION,
        MODIFICATION,
    }

    public class DirectoryChangeEvent {
        public UpdateType type;
        public Vector<DirectoryEntry> list;
        public int pos;

        @Contract(pure = true)
        DirectoryChangeEvent(UpdateType updateType, Vector<DirectoryEntry> entryVector, int pos) {
            this.type = updateType;
            this.list = entryVector;
            this.pos = pos;
        }
    }

}