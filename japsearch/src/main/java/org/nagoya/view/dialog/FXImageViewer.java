package org.nagoya.view.dialog;

import io.vavr.collection.Stream;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import org.nagoya.App;
import org.nagoya.GUICommon;
import org.nagoya.model.dataitem.FxThumb;
import org.nagoya.system.Systems;

public class FXImageViewer {

    public static void show(@NotNull Stream<FxThumb> imageList) {
        if (imageList.length() < 1) {
            return;
        }

        int rowCount = 0;
        int colCount = 0;

        GridPane gpane = new GridPane();
        gpane.setAlignment(Pos.CENTER);
        gpane.setVgap(0);
        gpane.setHgap(0);

        final double num = imageList.length();
        double cal = Math.sqrt(num);
        cal = cal + (cal / 10);

        final int MAX_COL = ((int) cal) + 1;
        final int MAX_ROW = (int) cal;

        final double maxWidth = App.getCurrentStage().map(Window::getWidth).getOrElse((double) 400) - 70;
        final double maxHeight = App.getCurrentStage().map(Window::getHeight).getOrElse((double) 300) - 170;

        GUICommon.debugMessage(()->String.valueOf((int) maxWidth / MAX_COL));
        GUICommon.debugMessage(()->String.valueOf((int) maxHeight / MAX_ROW));

        for (FxThumb thumb : imageList) {
            if (thumb.notEmpty()) {
                ImageView view = new ImageView();
                GUICommon.loadImageFromURL(thumb.getThumbURL(), maxWidth / MAX_COL, maxHeight / MAX_ROW, view::setImage);
                GridPane.setHalignment(view, HPos.CENTER);
                view.setOnMouseClicked((e) -> {
                    ImageView viewBig = new ImageView();
                    thumb.fitInImageView(viewBig, io.vavr.control.Option.of(maxWidth), io.vavr.control.Option.of(maxHeight));

                    Systems.getDialogPool().showDialog(thumb.getThumbURL().toString(), viewBig, "Close", null, null);
                });
                gpane.add(view, colCount, rowCount);
                colCount++;

                if (colCount > (MAX_COL - 1)) {
                    rowCount++;
                    colCount = 0;
                }
                if (rowCount >= MAX_ROW) {
                    break;
                }
            }
        }

        Systems.getDialogPool().showDialog("", gpane, "Close", null, null);
    }
}
