package org.nagoya.view.dialog;

import com.jfoenix.controls.JFXButton;
import io.vavr.collection.Stream;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import org.nagoya.App;
import org.nagoya.GUICommon;
import org.nagoya.model.dataitem.FxThumb;
import org.nagoya.system.Systems;

import java.util.ArrayList;
import java.util.function.Consumer;

public class FXImageViewer {

    public static void show(@NotNull Stream<FxThumb> imageList) {
        if (imageList.length() < 1) {
            return;
        }

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

        //GUICommon.debugMessage(() -> String.valueOf((int) maxWidth / MAX_COL));
        //GUICommon.debugMessage(() -> String.valueOf((int) maxHeight / MAX_ROW));

        ArrayList<FxThumb> thumbArray = new ArrayList<>();
        counter.index = 0;
        counter.colCount = 0;
        counter.rowCount = 0;

        imageList.forEach(t -> {
            ImageView view = new ImageView();

            if (t.isLocal()) {
                GUICommon.loadImageFromLocal(t.getLocalPath(), maxWidth / MAX_COL, maxHeight / MAX_ROW, view::setImage);
            } else {
                GUICommon.loadImageFromURL(t.getThumbURL(), maxWidth / MAX_COL, maxHeight / MAX_ROW, view::setImage);
            }
            GridPane.setHalignment(view, HPos.CENTER);

            thumbArray.add(t);

            int indexOfThis = counter.index;

            view.setOnMouseClicked((e) -> {
                ImageView viewBig = new ImageView();
                var popcurrent = new Object() {
                    int index = indexOfThis;
                };
                //GUICommon.debugMessage("index " + popcurrent.index);

                Consumer<Integer> showImage = (i) ->
                {
                    thumbArray.get(i).fitInImageView(viewBig, io.vavr.control.Option.of(maxWidth), io.vavr.control.Option.of(maxHeight));
                };

                showImage.accept(popcurrent.index);

                JFXButton bPrev = GUICommon.getBorderButton("<", (e1) -> {
                    if (popcurrent.index > 0) {
                        popcurrent.index--;
                    }
                    showImage.accept(popcurrent.index);
                });

                JFXButton bNext = GUICommon.getBorderButton(">", (e2) -> {
                    if (popcurrent.index < (thumbArray.size() - 1)) {
                        popcurrent.index++;
                    }
                    showImage.accept(popcurrent.index);
                });

                HBox hBox = GUICommon.getHBox(10, bPrev, viewBig, bNext);
                Systems.getDialogPool().showDialog("", hBox, "Close", null, null);
            });

            gpane.add(view, counter.colCount, counter.rowCount);

            counter.index++;
            counter.colCount++;

            if (counter.colCount > (MAX_COL - 1)) {
                counter.rowCount++;
                counter.colCount = 0;
            }
        });

        Systems.getDialogPool().showDialog("", gpane, "Close", null, null);
    }

    static class counter {
        static int index = 0;
        static int rowCount = 0;
        static int colCount = 0;
    }
}
