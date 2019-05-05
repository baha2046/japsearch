package org.nagoya.view.customcell;

import javafx.scene.control.TreeCell;

import java.nio.file.Path;

public class PathListTreeCell extends TreeCell<Path> {


    public PathListTreeCell() {
    }

    @Override
    public void updateItem(Path data, boolean empty) {
        super.updateItem(data, empty);
        if (empty) {
            //空の場合は、ラベルもアイコンも表示させない
            this.setText(null);
            this.setGraphic(null);
        } else if (this.isEditing()) {
            //編集時はLabeledTextにラベルを表示させない
            this.setText(null);
            this.setGraphic(this.getTreeItem().getGraphic());
        } else {
            //通常の表示
            this.setText(data.getFileName().toString());
            this.setGraphic(this.getTreeItem().getGraphic());
        }
    }
}
