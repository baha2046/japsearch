module org.nagoya {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.nagoya to javafx.fxml;
    exports org.nagoya;
}