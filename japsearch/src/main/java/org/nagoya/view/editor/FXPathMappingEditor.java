package org.nagoya.view.editor;

import io.vavr.collection.Vector;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.nagoya.GUICommon;
import org.nagoya.preferences.RenameSettings;


public class FXPathMappingEditor {
    public static void show(Runnable runnable) {

        Vector<MappingData> dataVector = Vector.of(RenameSettings.getInstance().getCompany())
                .map(t->new MappingData(t.substring(0,t.indexOf("|") ),t.substring(t.indexOf("|")+1 )));

        //dataVector.forEach(t->GUICommon.debugMessage(t.toString()));

        TableView<MappingData> tableView = new TableView<>();

        TableColumn<MappingData, String> fromColumn = new TableColumn<>("From");
        TableColumn<MappingData, String> toColumn = new TableColumn<>("To");

        fromColumn.setPrefWidth(250);
        toColumn.setPrefWidth(250);
        fromColumn.setCellValueFactory(
                new PropertyValueFactory<>("fromString"));
        toColumn.setCellValueFactory(
                new PropertyValueFactory<>("toString"));
        fromColumn.setCellFactory(arg0 -> new TextFieldTableCell<>(new DefaultStringConverter()));
        fromColumn.setOnEditCommit(t ->
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setFromString(t.getNewValue()));
        toColumn.setCellFactory(arg0 -> new TextFieldTableCell<>(new DefaultStringConverter()));
        toColumn.setOnEditCommit(t ->
                t.getTableView().getItems().get(t.getTablePosition().getRow()).setToString(t.getNewValue()));

        tableView.setMinWidth(520);
        tableView.setMaxHeight(500);
        tableView.setMinHeight(500);
        tableView.setEditable(true);
        //noinspection unchecked
        tableView.getColumns().addAll(fromColumn, toColumn);

        ObservableList<MappingData> dataObservableList = FXCollections.observableArrayList(dataVector.asJava());
        tableView.setItems(dataObservableList);

        var btnDel = GUICommon.getButton("[Delete Selected Mapping]", (e)->{
            if(tableView.getSelectionModel().getSelectedIndex() != -1)
                dataObservableList.remove(tableView.getSelectionModel().getSelectedIndex());
        });

        var vBox = GUICommon.getVBox(15,tableView, btnDel);

        GUICommon.showDialog("Mapping editor :", vBox, "Cancel", "Save", ()->{
            RenameSettings.getInstance()
                    .setCompany(Vector.ofAll(dataObservableList)
                            .map(m-> m.fromStringProperty().get() + "|" + m.toStringProperty().get())
                            .toJavaArray(String.class));
            RenameSettings.getInstance().writeSetting();
            runnable.run();
        });
    }

    public static class MappingData {
        private final StringProperty fromString;
        private final StringProperty toString;

        MappingData(String f, String t)
        {
            fromString = new SimpleStringProperty(f);
            toString =  new SimpleStringProperty(t);
        }

        public StringProperty fromStringProperty() {return fromString;}
        public StringProperty toStringProperty() {return toString;}
        void setToString(String t) {toString.set(t);}
        void setFromString(String f) {fromString.set(f);}
    }

}
