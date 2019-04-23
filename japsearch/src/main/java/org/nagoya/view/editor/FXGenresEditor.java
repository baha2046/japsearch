package org.nagoya.view.editor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.model.dataitem.Genre;


public class FXGenresEditor  {

    private static void init(ObservableList<String> observableData, GridPane gridPane) {

        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(5);
        gridPane.setVgap(10);

        //private ObservableList<String> observableData = FXCollections.observableArrayList();
        //private ObservableList<Genre> genreListIn;
        ListView<String> simpleList = new ListView<>(observableData);
        simpleList.setEditable(true);
        simpleList.setPrefHeight(160);
        simpleList.setPrefWidth(190);

        simpleList.setCellFactory(lv -> new ListCell<String>() {
            private TextField textField = new TextField();

            {
                this.textField.setOnAction(e -> this.commitEdit(this.getItem()));

                this.textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        observableData.set(this.getIndex(), this.textField.getText());
                        System.out.println("Commiting " + this.textField.getText());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    this.setText(null);
                    this.setGraphic(null);
                } else if (this.isEditing()) {
                    this.textField.setText(item);
                    this.setText(null);
                    this.setGraphic(this.textField);
                } else {
                    this.setText(item);
                    this.setGraphic(null);
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                this.textField.setText(this.getItem());
                this.setText(null);
                this.setGraphic(this.textField);
                this.textField.selectAll();
                this.textField.requestFocus();
            }
        });

        simpleList.setOnEditCommit(t -> {
            //FXGenresEditor.this.simpleList.getItems().set(t.getIndex(), t.getNewValue());
            System.out.println("setOnEditCommit");
        });

        simpleList.setOnEditCancel(t -> System.out.println("setOnEditCancel"));

        gridPane.add(simpleList, 0, 0, 3, 1);

        Button btnAdd = new Button("Add");
        btnAdd.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
        {
            observableData.add("NEW");
        });

        gridPane.add(btnAdd, 0, 1);
    }

    public static void show(@NotNull ObservableList<Genre> genreObservableList) {

        ObservableList<String> observableData = FXCollections.observableArrayList();

        for (Genre i : genreObservableList) {
            observableData.add(i.getGenre());
        }

        GridPane gridPane = new GridPane();
        init(observableData, gridPane);

        GUICommon.showDialog("Genres editor :", gridPane, "Cancel", "Appy Change", () -> {
            genreObservableList.clear();
            for (String i : observableData) {
                i = i.replace("\n", "").replace("\r", "");
                if (!i.equalsIgnoreCase("")) {
                    System.out.println("setOnEditCommit " + i);
                    genreObservableList.add(new Genre(i));
                }
            }
        });
    }
}
