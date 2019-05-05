package org.nagoya.system;

import javafx.fxml.Initializable;
import javafx.scene.Node;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class FXMLController implements Initializable {
    protected Node pane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public Node getPane() {
        return this.pane;
    }

    @SuppressWarnings("unchecked")
    public <T extends FXMLController> T setPane(Node pane) {
        this.pane = pane;
        return (T) this;
    }
}
