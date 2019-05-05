package org.nagoya.controller;

import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.nagoya.App;
import org.nagoya.GUICommon;
import org.nagoya.system.FXContextImp;
import org.nagoya.system.event.CustomEvent;
import org.nagoya.system.event.CustomEventType;

public class FXTaskBarControl extends FXContextImp {

    public static final CustomEventType EVENT_ADD_TASK = new CustomEventType("EVENT_ADD_TASK");
    public static final CustomEventType EVENT_REMOVE_TASK = new CustomEventType("EVENT_REMOVE_TASK");

    private static FXTaskBarControl instance = null;

    private HBox hBox;
    private JFXButton taskButton;

    private FXTaskBarControl() {
        this.pane = new AnchorPane();
        ((AnchorPane) this.pane).setPrefHeight(50);
        this.hBox = GUICommon.getHBox(15);
        this.hBox.setPadding(new Insets(5));
        ((AnchorPane) this.pane).getChildren().add(this.hBox);

        JFXButton javButton = GUICommon.getBorderButton("JAV", (e)->{
            App.getMainScreen().setCenter(FXMoviePanelControl.getInstance().getPane());
        });

        JFXButton mangaButton = GUICommon.getBorderButton("Manga", (e)->{
            App.getMainScreen().setCenter(FXMangaPanelControl.getInstance().getPane());
        });

        this.taskButton = GUICommon.getBorderButton("Task :                            ", (e) -> {
        });
        this.taskButton.setTextFill(Paint.valueOf("WHITE"));
        this.taskButton.setVisible(false);

        this.hBox.getChildren().addAll(javButton, mangaButton, this.taskButton);
    }

    public static FXTaskBarControl getInstance() {
        if (instance == null) {
            instance = new FXTaskBarControl();
        }
        return instance;
    }

    @Override
    public void registerListener() {
        this.registerListener(EVENT_ADD_TASK);
        this.registerListener(EVENT_REMOVE_TASK);
    }

    @Override
    public void executeEvent(CustomEvent e) {
        if (e.getType().equals(EVENT_ADD_TASK)) {
            this.addTask((String) e.getObject());
        } else if (e.getType().equals(EVENT_REMOVE_TASK)) {
            this.removeTask((String) e.getObject());
        }
    }

    public boolean isTaskRunning() {
        return this.taskButton.isVisible();
    }

    private void addTask(String taskString) {
        this.taskButton.setText("Task : " + taskString);
        this.taskButton.setVisible(true);
    }

    private void removeTask(String taskString) {
        if (this.taskButton.getText().endsWith(taskString)) {
            this.taskButton.setVisible(false);
        }
    }
}
