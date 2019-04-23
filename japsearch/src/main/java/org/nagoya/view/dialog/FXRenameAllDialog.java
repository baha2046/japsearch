package org.nagoya.view.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import io.vavr.control.Option;
import javafx.collections.ObservableList;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.nagoya.GUICommon;
import org.nagoya.UtilCommon;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.preferences.RenameSettings;
import org.nagoya.system.Systems;
import org.nagoya.view.editor.FXRenameFormatEditor;

import java.nio.file.Path;

public class FXRenameAllDialog {

    public static void show()
    {
        JFXListView<String> textArea = GUICommon.getTextArea(500, 500, true);

        JFXButton btnEdit = GUICommon.getBorderButton("Setting", e -> {
            FXRenameFormatEditor.show(FXRenameFormatEditor.Type.DIRECTORY_NAME);
        });

        JFXButton btnRun = GUICommon.getOkButton("Rename All", null);

        btnRun.setOnAction(e -> {

            textArea.getItems().clear();
            Option<ObservableList<String>> outs = Option.of(textArea.getItems());
            ObservableList<DirectoryEntry> directoryEntries = Systems.getDirectorySystem().getDirectoryEntries();

            Systems.useExecutors(()-> {
                boolean bReload = false;
                for (int x = 0; x < directoryEntries.size(); x++) {
                    DirectoryEntry directoryEntry = directoryEntries.get(x);
                    if (directoryEntry.isDirectory() && directoryEntry.hasMovie() && directoryEntry.hasNfo()) {
                        Path newPath = directoryEntry.getFilePath().getParent().resolve(RenameSettings.getSuitableDirectoryName(directoryEntry.getMovieData()).trim());
                        if (!newPath.equals(directoryEntry.getFilePath())) {
                            bReload = true;
                            if (UtilCommon.renameFile(directoryEntry.getFilePath(), newPath)) {
                                directoryEntry.clearCache();
                                directoryEntries.set(x, DirectoryEntry.getAndExe(newPath));
                            }
                            GUICommon.writeToObList(newPath.getFileName().toString(), outs);
                        } else {
                            GUICommon.writeToObList("Skip - same name (" + newPath.getFileName().toString() + ")", outs);
                        }
                    }
                }
                GUICommon.writeToObList("Finish", outs);
                if(bReload) Systems.getDirectorySystem().reloadDirectory();
            });
        });

        VBox vBox = GUICommon.getVBox(15, textArea, new Separator(), btnEdit, btnRun);
        vBox.setMinWidth(502);

        GUICommon.showDialog("Rename All Movie :", vBox, "Done", null, null);
    }
}
