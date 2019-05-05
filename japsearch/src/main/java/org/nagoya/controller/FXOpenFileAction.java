package org.nagoya.controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import org.nagoya.App;
import org.nagoya.GUICommon;
import org.nagoya.model.DirectoryEntry;

import java.awt.*;
import java.io.IOException;

public class FXOpenFileAction {

    /**
     * @param directoryEntry
     */
    public static void run(@NotNull DirectoryEntry directoryEntry)
    {
        String type = directoryEntry.getFileExtenion();
        if (type == null) {
            type = "";
        }
        int x = type.indexOf("/");

        if (x > 0) {
            type = type.substring(0, x);
        }

        switch (type) {
            case "image":
                try {
                    javafx.scene.image.Image fximage = new Image(directoryEntry.getFilePath().toUri().toURL().toString());
                    ImageView pic = new ImageView();
                    pic.setImage(fximage);
                    double maxWidth = App.getCurrentStage().map(Window::getWidth).getOrElse((double) 300) - 60;
                    double maxHeight = App.getCurrentStage().map(Window::getHeight).getOrElse((double) 400) - 180;
                    if (fximage.getWidth() > maxWidth) {
                        pic.setFitWidth(maxWidth);
                    }
                    if (fximage.getHeight() > maxHeight) {
                        pic.setFitHeight(maxHeight);
                    }
                    pic.setPreserveRatio(true);
                    GUICommon.showDialog(directoryEntry.getFilePath().toString(), pic, "Close", null, null);
                } catch (Exception e) {
                    System.out.println("JavaFX doesn't support");
                }
                break;
            default:
                try {
                    Desktop.getDesktop().open(directoryEntry.getFilePath().toFile());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
        }
    }
}