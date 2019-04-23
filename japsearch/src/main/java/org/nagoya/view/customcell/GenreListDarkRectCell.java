package org.nagoya.view.customcell;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;

public class GenreListDarkRectCell extends GenreListRectCell {
    public GenreListDarkRectCell() {
        this.setTextFill(Color.web("white"));
        this.setStyle("-fx-effect: dropshadow( three-pass-box, rgba(0,0,0,0.4), 3, 0.0, 2, 2); -fx-font-size: 14; -fx-font-weight: bold;");
        this.setAlignment(Pos.TOP_LEFT);
        this.setPadding(new Insets(10, 0, 5, 0));
    }
}
