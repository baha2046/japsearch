module org.nagoya {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.swing;
    requires transitive javafx.web;

    requires reactfx;
    requires livedirsfx;
    requires org.controlsfx.controls;
    requires com.jfoenix;
    requires de.jensd.fx.glyphs.fontawesome;

    requires org.jetbrains.annotations;
    requires java.desktop;
    requires io.vavr;
    requires cyclops;
    requires io.reactivex.rxjava2;
    requires gson;
    requires io.vavr.gson;
    requires java.sql;
    requires com.github.benmanes.caffeine;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.commons.text;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires xstream;
    requires org.jsoup;
    requires commons.codec;
    requires org.reactivestreams;

    opens org.nagoya to javafx.fxml;
    opens org.nagoya.view to javafx.fxml;
    opens org.nagoya.view.customcell to javafx.fxml;
    opens org.nagoya.controller to javafx.fxml;

    opens org.nagoya.view.editor to javafx.base;

    opens org.nagoya.model.xmlserialization to xstream;
    opens org.nagoya.preferences to xstream;

    exports org.nagoya;
    exports org.nagoya.view;
    exports org.nagoya.system;
    exports org.nagoya.system.event;
    exports org.nagoya.model;
    exports org.nagoya.model.dataitem;
    exports org.nagoya.preferences;
    exports org.nagoya.controller;
}