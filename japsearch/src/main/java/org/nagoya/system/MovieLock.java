package org.nagoya.system;

import io.vavr.collection.Vector;
import org.nagoya.GUICommon;

import java.nio.file.Path;

public class MovieLock {

    private static final MovieLock instance = new MovieLock();

    private Vector<Path> lockList = Vector.empty();

    public static MovieLock getInstance() {
        return instance;
    }

    public void addToList(Path path) {
        if (this.lockList.find(p -> p.equals(path)).isEmpty()) {
            this.lockList = this.lockList.append(path);

            GUICommon.debugMessage("Add to Lock >> " + path.toString());
        }
    }

    public void removeFromList(Path path) {
        this.lockList = this.lockList.remove(path);
    }

    public boolean notInList(Path path) {
        return this.lockList.find(p -> p.equals(path)).isEmpty();
    }
}
