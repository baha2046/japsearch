package org.nagoya.model;

import io.vavr.collection.Vector;
import io.vavr.control.Option;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;
import org.nagoya.UtilCommon;
import org.nagoya.model.dataitem.FxThumb;

public class GalleryFolder {

    private static final int NUM_JPG_REQUIRE = 6;

    private final Vector<FxThumb> galleryImages;

    @NotNull
    public static Option<GalleryFolder> create(DirectoryEntry entry) {

        if (entry == null) {
            return Option.none();
        }

        var g = isGallery(entry);

        return g.count(i -> true) > NUM_JPG_REQUIRE ? Option.of(new GalleryFolder(g)) : Option.none();
    }

    private static Vector<FxThumb> isGallery(@NotNull DirectoryEntry entry) {
        return entry.getChildrenEntry()
                .map(TreeItem::getValue)
                .filter(path -> UtilCommon.checkFileExt.test(path, ".jpg"))
                .map(FxThumb::of)
                ;

        //.peek(p->GUICommon.debugMessage(p.getFileNameProperty().toString()))

    }

    private GalleryFolder(Vector<FxThumb> g) {
        this.galleryImages = g;
    }

    public Vector<FxThumb> getGalleryImages() {
        return this.galleryImages;
    }
}
