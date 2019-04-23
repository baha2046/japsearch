package org.nagoya.system;

import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.UtilCommon;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.system.cache.DirectoryCacheInstance;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DirectorySystem {
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

    private final ObservableList<DirectoryEntry> directoryEntries;

    private final Map<String, Double> scrollPosMap;

    private Path currentPath;

    public DirectorySystem() {
        this.currentPath = null;
        this.directoryEntries = FXCollections.observableArrayList(DirectoryEntry.extractor());
        this.currentScrollbarPos = new SimpleDoubleProperty(0);
        this.scrollPosMap = new LinkedHashMap<>();

        //this.currentDirectoryStringProperty = new SimpleStringProperty();
        //this.currentDirectoryStringProperty.addListener((ChangeListener<? super String>) (observable, oldValue, newValue) -> runnable.accept(this.directoryEntries));
    }

    public static Vector<Path> readPath(Path path) {
        Benchmark b = new Benchmark(false, "ReadPath " + path.toString());

        Try<DirectoryStream<Path>> streamTry = Try.of(() -> Files.newDirectoryStream(path));

        Vector<Path> pathList = Vector.ofAll(streamTry
                .map((s) -> StreamSupport.stream(s.spliterator(), false).collect(Collectors.toList()))
                .getOrElse(ArrayList::new));

        streamTry.peek(s -> Try.run(s::close));

        b.B("End > ");

        return pathList;
    }

    public static List<DirectoryEntry> loadPath(@NotNull Path path) {
        return readPath(path)/*.sorted(COMP_FOR_PATH)*/.map(DirectoryEntry::of).toJavaList();
    }

    public void sortContain() {
        this.directoryEntries.sort(COMP_FOR_DIR);
    }

    public void changePathTo(Path newPath, Consumer<Path> run) {
        this.changePathTo(newPath, run, false);
    }

    public void changePathTo(int index, Consumer<Path> run) {
        Path path = this.directoryEntries.get(index).getFilePath();
        this.changePathTo(path, run);
    }

    public void changePathTo(Path newPath, Consumer<Path> run, boolean reload) {
        Systems.usePriorityExecutors(() -> {

            if (newPath != null && Files.isDirectory(newPath)) {

                GUICommon.debugMessage(() -> "DirectorySystem -- Change to Directory : " + newPath.toString());

                if (reload) {
                    DirectoryCacheInstance.cache().evict(newPath);
                } else if (this.currentPath != null) {
                    DirectoryCacheInstance.cache().put(this.currentPath, new ArrayList<>(this.directoryEntries));
                    this.scrollPosMap.put(this.currentPath.toString(), this.currentScrollbarPos.get());
                }

                this.currentPath = newPath;
                List<DirectoryEntry> directoryEntries = DirectoryCacheInstance.cache().get(newPath);

                Platform.runLater(() -> {
                    GUICommon.debugMessage(() -> "Show Directory Change");
                    this.directoryEntries.clear();

                    this.directoryEntries.addAll(directoryEntries);
                    if (run != null) {
                        run.accept(newPath);
                    }

                    Future<Seq<Boolean>> seqFuture = Future.sequence(Stream.ofAll(directoryEntries).map(d -> Future.of(Systems.getExecutorServices(), d)));
                    seqFuture.onComplete((b) -> {
                        Platform.runLater(this::sortContain);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        this.currentScrollbarPos.set(this.scrollPosMap.getOrDefault(this.currentPath.toString(), 0.0));
                    });
                });
            }
        });
    }

    public void upDirectory(Consumer<Path> run) {
        if (this.currentPath != null && this.currentPath.getParent() != null) {
            this.changePathTo(this.currentPath.getParent(), run);
        }
    }

    public void renameFile(DirectoryEntry directoryEntry, @NotNull Path newPath) {

        int index = this.directoryEntries.indexOf(directoryEntry);

        if (index != -1 && UtilCommon.renameFile(directoryEntry.getFilePath(), newPath)) {
            directoryEntry.clearCache();
            DirectoryEntry newEntry = new DirectoryEntry(newPath);
            this.directoryEntries.set(index, newEntry);
            Systems.useExecutors(newEntry);
        }
    }

    public void deleteFile(int index) {
        DirectoryEntry entry = this.directoryEntries.get(index);

        UtilCommon.delFile(entry.getFilePath(), ()->{
            entry.clearCache();
            this.removeEntry(index);
        });
    }

    public void removeEntry(int index) {
        this.directoryEntries.remove(index);
    }

    public void removeEntry(DirectoryEntry directoryEntry) {
        this.directoryEntries.remove(directoryEntry);
    }

    public void reloadDirectory() {
        this.changePathTo(this.currentPath, null, true);
    }

    public ObservableList<DirectoryEntry> getDirectoryEntries() {
        return this.directoryEntries;
    }

    public Path getCurrentPath() {
        return this.currentPath;
    }
}