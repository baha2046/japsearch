package org.nagoya.view.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import io.vavr.control.Option;
import javafx.collections.ObservableList;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.nagoya.GUICommon;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.preferences.RenameSettings;
import org.nagoya.system.Systems;
import org.nagoya.view.editor.FXRenameFormatEditor;

import java.nio.file.Path;

public class FXRenameAllDialog {

    public static void show() {
        JFXListView<String> textArea = GUICommon.getTextArea(500, 500, true);

        JFXButton btnEdit = GUICommon.getBorderButton("Setting", e -> {
            FXRenameFormatEditor.show(FXRenameFormatEditor.Type.DIRECTORY_NAME);
        });

        JFXButton btnRun = GUICommon.getOkButton("Rename All", null);

        btnRun.setOnAction(e -> {
            textArea.getItems().clear();
            Option<ObservableList<String>> outs = Option.of(textArea.getItems());

            Systems.useExecutors(() -> {
                Systems.getDirectorySystem()
                        .getDirectoryEntries()
                        .filter(DirectoryEntry::isDirectory)
                        .filter(DirectoryEntry::hasNfo)
                        .forEach(d ->
                        {
                            Path newPath = d.getValue().resolveSibling(RenameSettings.getSuitableDirectoryName(d.getMovieData()).trim());
                            if (!newPath.equals(d.getFilePath())) {
                                Systems.getDirectorySystem().renameFile(d, newPath);
                                GUICommon.writeToObList(newPath.getFileName().toString(), outs);
                            } else {
                                GUICommon.writeToObList("Skip - same name (" + newPath.getFileName().toString() + ")", outs);
                            }
                        });
                GUICommon.writeToObList("Finish", outs);
            });
        });

        VBox vBox = GUICommon.getVBox(15, textArea, new Separator(), btnEdit, btnRun);
        vBox.setMinWidth(502);

        GUICommon.showDialog("Rename All Movie :", vBox, "Done", null, null);
    }
}
