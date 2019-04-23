module org.nagoya {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;
    requires fontawesomefx;
    requires org.controlsfx.controls;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires io.vavr;
    requires cyclops;
    requires javafx.swing;
    requires reactive.streams;
    requires gson;
    requires io.vavr.gson;
    requires com.github.benmanes.caffeine;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires xstream;

    opens org.nagoya to javafx.fxml;
    exports org.nagoya;
}