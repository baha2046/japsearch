package org.nagoya.view;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.nagoya.App;
import org.nagoya.model.dataitem.ActorV2;
import org.nagoya.model.dataitem.Genre;
import org.nagoya.system.FXMLController;
import org.nagoya.view.customcell.ActorListRectCell;
import org.nagoya.view.customcell.GenreListDarkRectCell;
import org.nagoya.view.editor.FXGenresEditor;

import java.net.URL;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

public class FXMovieDetailView extends FXMLController {

    //@FXML
    @FXML
    protected JFXTextField txt3;
    @FXML
    protected JFXTextField txt1, txt2, txt4, txt5, txt6;
    @FXML
    protected JFXTextArea txt7;
    @FXML
    protected JFXListView<ActorV2> lvActors;
    @FXML
    protected JFXListView<Genre> lvGenres;
    @FXML
    protected JFXTextField txtD, txtM;

    public FXMovieDetailView() {
    }

    static String normalize(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFKC);
    }

    void bindData(Map<String, SimpleStringProperty> customDataMap, ObservableList<ActorV2> actorObservableList, ObservableList<Genre> genreObservableList) {

        String pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        //Bindings.bindBidirectional(customDataMap.get("Date"), this.txt3.valueProperty(), new LocalDateStringConverter(formatter, null));
        this.txt3.textProperty().bindBidirectional(customDataMap.get("Date"));
        this.txt1.textProperty().bindBidirectional(customDataMap.get("Title"));
        this.txt2.textProperty().bindBidirectional(customDataMap.get("Year"));
        this.txt4.textProperty().bindBidirectional(customDataMap.get("ID"));
        this.txt5.textProperty().bindBidirectional(customDataMap.get("Studio"));
        this.txt6.textProperty().bindBidirectional(customDataMap.get("Set"));
        this.txt7.textProperty().bindBidirectional(customDataMap.get("Plot"));
        this.txtD.textProperty().bindBidirectional(customDataMap.get("Director"));
        this.txtM.textProperty().bindBidirectional(customDataMap.get("Maker"));

        //this.lvActors.setItems(actorObservableList);
        this.lvGenres.setItems(genreObservableList);
    }

    void setActorList(ObservableList<ActorV2> actorObservableList) {
        this.lvActors.setItems(actorObservableList);
    }

    @FXML
    private void addActorAction() {
        this.lvActors.getItems().add(ActorV2.of("Actor", ActorV2.Source.NONE,"", "", ""));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.lvGenres.setCellFactory((ListView<Genre> l) -> new GenreListDarkRectCell());
        this.lvActors.setCellFactory((ListView<ActorV2> l) -> new ActorListRectCell());
        this.lvGenres.setExpanded(true);

        this.lvGenres.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                FXGenresEditor.show(this.lvGenres.getItems());
            }
        });
    }


    class ActorEditor extends GridPane {

        private final Stage dialog = new Stage(StageStyle.TRANSPARENT);
        ObservableList<String> observableData = FXCollections.observableArrayList();
        ObservableList<ActorV2> actorListIn;
        private ListView<String> simpleList;

        ActorEditor() {

        }

        public void Init() {

            this.setAlignment(Pos.CENTER);
            this.setHgap(5);
            this.setVgap(10);

            final Group root = new Group();
            final Scene scene = new Scene(root, 240, 280, Color.TRANSPARENT);

            this.dialog.initModality(Modality.WINDOW_MODAL);
            this.dialog.setResizable(false);
            this.dialog.initOwner(App.getCurrentStage().getOrNull());

            this.dialog.showingProperty().addListener((observableValue, wasShowing, isShowing) ->
            {
                //system.out.println("this.dialog.showingProperty().addListener((observableValue, wasShowing, isShowing) -> >> " + isShowing);
                App.getCurrentStage().map(Window::getScene).map(Scene::getRoot).peek(r->r.setEffect(isShowing ? new BoxBlur() : null));
            });

            this.simpleList = new ListView<>(this.observableData);
            this.simpleList.setEditable(true);
            this.simpleList.setPrefHeight(160);
            this.simpleList.setPrefWidth(190);
            this.simpleList.setCellFactory(TextFieldListCell.forListView());

            this.simpleList.setOnEditCommit(t -> {
                ActorEditor.this.simpleList.getItems().set(t.getIndex(), t.getNewValue());
                System.out.println("setOnEditCommit");
            });

            this.simpleList.setOnEditCancel(t -> System.out.println("setOnEditCancel"));

            this.add(this.simpleList, 0, 0, 3, 1);

            Button btnAdd = new Button("Add");
            Button yes = new Button("Apply Change");
            Button no = new Button("Cancel");

            btnAdd.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
            {
                this.observableData.add("NEW");
            });

            yes.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
            {
                this.actorListIn.clear();
                for (String i : this.observableData) {
                    i = i.replace("\n", "").replace("\r", "");
                    if (!i.equalsIgnoreCase("")) {
                        System.out.println("setOnEditCommit " + i);
                        //this.actorListIn.add(new Genre(i));
                    }
                }
                this.dialog.close();
            });

            no.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
            {
                this.dialog.close();
            });

            this.add(btnAdd, 0, 1);
            this.add(yes, 1, 1);
            this.add(no, 2, 1);

            root.getChildren().add(this);
            this.dialog.setScene(scene);
        }

        public void show(ObservableList<ActorV2> actorObservableList) {

            this.actorListIn = actorObservableList;

            this.dialog.setX((App.getCurrentStage().map(Window::getWidth).getOrElse((double) 400) / 2)
                    + App.getCurrentStage().map(Window::getX).getOrElse((double) 0));
            this.dialog.setY((App.getCurrentStage().map(Window::getHeight).getOrElse((double) 400) / 2)
                    + App.getCurrentStage().map(Window::getY).getOrElse((double) 0));

            this.observableData.clear();
            for (ActorV2 i : actorObservableList) {
                this.observableData.add(i.getName());
            }
            this.dialog.show();
        }
    }
}




