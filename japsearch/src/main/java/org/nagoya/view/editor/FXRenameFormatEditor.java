package org.nagoya.view.editor;

import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.nagoya.GUICommon;
import org.nagoya.model.MovieV2;
import org.nagoya.preferences.RenameSettings;

public class FXRenameFormatEditor {

    public static void show(Type type) {
        JFXListView<String> element = new JFXListView<>();
        JFXListView<String> elementSelected = new JFXListView<>();

        ObservableList<String> elemantList = FXCollections.observableArrayList();
        ObservableList<String> elemantSelectedList = FXCollections.observableArrayList();

        elemantList.addAll(MovieV2.getSupportRenameElement());
        if (type == Type.DIRECTORY_NAME) {
            elemantSelectedList.addAll(RenameSettings.getInstance().getRenameDirectoryFormat());
        } else {
            elemantSelectedList.addAll(RenameSettings.getInstance().getRenameFileFormat());
        }

        element.setOrientation(Orientation.HORIZONTAL);
        element.setMinWidth(800);
        element.setMinHeight(60);
        element.setMaxHeight(60);
        elementSelected.setOrientation(Orientation.HORIZONTAL);
        elementSelected.setMinWidth(800);
        elementSelected.setMinHeight(60);
        elementSelected.setMaxHeight(60);
        element.setItems(elemantList);
        elementSelected.setItems(elemantSelectedList);

        element.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                elemantSelectedList.add(elemantList.get(element.getSelectionModel().getSelectedIndex()));
            }
        });

        elementSelected.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                if (elemantSelectedList.size() > 1) {
                    elemantSelectedList.remove(elementSelected.getSelectionModel().getSelectedIndex());
                }
            }
        });

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPrefWidth(850);

        vBox.getChildren().addAll(new Text("Element Support"), element, new Text("Current Format"), elementSelected);

        GUICommon.showDialog("Formatter :", vBox, "Cancel", "Apply", () -> {
            if (type == Type.DIRECTORY_NAME) {
                RenameSettings.getInstance().setRenameDirectoryFormat(elemantSelectedList.toArray(new String[0]));
            } else {
                RenameSettings.getInstance().setRenameFileFormat(elemantSelectedList.toArray(new String[0]));
            }
            RenameSettings.getInstance().writeSetting();
        });
    }

    public enum Type {
        DIRECTORY_NAME, FILE_NAME
    }
}
