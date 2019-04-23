package org.nagoya.view.customcell;

import javafx.scene.control.ListCell;
import org.nagoya.model.dataitem.Genre;

public class GenreListRectCell extends ListCell<Genre> {
    @Override
    public void updateItem(Genre item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            this.setText(item.getGenre());
            //this.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, 12));
            this.setGraphic(null);
        } else {
            this.setGraphic(null);
            this.setText("");
        }
    }
}
