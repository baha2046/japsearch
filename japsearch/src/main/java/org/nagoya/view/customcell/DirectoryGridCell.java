package org.nagoya.view.customcell;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.controlsfx.control.GridCell;
import org.nagoya.GUICommon;
import org.nagoya.model.PathCell;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DirectoryGridCell extends GridCell<PathCell> implements Initializable {

    private final AnchorPane pane;
    private final Consumer<String> stringConsumer;

    @FXML
    JFXButton btn;

    @FXML
    Label name;

    public DirectoryGridCell(Consumer<String> consumer) {
        //this.setTextFill(Color.web("navy"));
        //this.setStyle("-fx-effect: dropshadow( three-pass-box, rgba(0,0,0,0.4), 3, 0.0, 2, 2); -fx-font-size: 14; -fx-font-weight: bold;");
        this.setAlignment(Pos.TOP_LEFT);
        // getChildren().add(rectangle2D);
        this.setWidth(180);
        this.setHeight(40);
        this.setPadding(new Insets(0, 0, 0, 0));

        stringConsumer = consumer;
        pane = new AnchorPane();
        GUICommon.loadFXMLRoot("/fxml/FXDirGridCell.fxml", this, pane);
    }

    @Override
    public void updateItem(PathCell item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            this.name.setText(item.getPath());
            if (item.getIsUse().get()) {
                this.name.setTextFill(Color.web("yellow"));
            } else {
                this.name.setTextFill(Color.web("white"));
            }
            //this.setFont(Font.font(Font.getDefault().getName(), FontWeight.NORMAL, 12));
            this.setGraphic(this.pane);
        } else {
            this.setGraphic(null);
            this.setText("");
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btn.setOnAction((e)->{
                    ((DirectoryGridCell)((JFXButton)e.getSource()).getParent().getParent())
                            .stringConsumer.accept(name.getText());
        });
    }
}
