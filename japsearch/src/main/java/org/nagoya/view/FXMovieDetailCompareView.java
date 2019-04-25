package org.nagoya.view;

import com.jfoenix.controls.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.nagoya.GUICommon;
import org.nagoya.model.MovieV2;
import org.nagoya.model.dataitem.*;
import org.nagoya.view.customcell.ActorListRectCell;
import org.nagoya.view.customcell.GenreListRectCell;
import org.nagoya.view.dialog.FXImageViewer;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class FXMovieDetailCompareView extends FXMovieDetailView {
    @FXML
    protected JFXTextField txt11, txt41, txt51, txt61;
    @FXML
    protected JFXTextField txt31;
    @FXML
    protected JFXTextArea txt71;
    @FXML
    protected JFXTextField txtD1, txtM1;
    @FXML
    private JFXListView<ActorV2> lvActors;
    @FXML
    private JFXListView<Genre> lvGenres;
    @FXML
    private JFXListView<ActorV2> lvActors1;
    @FXML
    private JFXListView<Genre> lvGenres1;
    @FXML
    private JFXToggleButton btn1, btn3, btn4, btn5, btn6, btn7;
    @FXML
    private JFXToggleButton btnA, btnG, btnD, btnM, btnUseCover, btnUseExtra;
    @FXML
    private JFXToggleButton btnAll;
    @FXML
    private Label txtExtraImage;
    @FXML
    private JFXButton btnExtraFanArt, btnCover;

    public FXMovieDetailCompareView() {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/FXMovieDetailCompare.fxml"));
        fxmlLoader.setController(this);
        //system.out.println("------------ FXMovieDetailCompareView ------------" + (this.getClass() == FXMovieDetailCompareView.class));
        try {
            this.pane = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.lvGenres.setCellFactory((ListView<Genre> l) -> new GenreListRectCell());
        this.lvActors.setCellFactory((ListView<ActorV2> l) -> new ActorListRectCell(false));
        this.lvGenres1.setCellFactory((ListView<Genre> l) -> new GenreListRectCell());
        this.lvActors1.setCellFactory((ListView<ActorV2> l) -> new ActorListRectCell(false));
        this.txt1.disableProperty().bind(this.btn1.selectedProperty());
        this.txt11.disableProperty().bind(this.btn1.selectedProperty().not());
        this.txt3.disableProperty().bind(this.btn3.selectedProperty());
        this.txt31.disableProperty().bind(this.btn3.selectedProperty().not());
        this.txt4.disableProperty().bind(this.btn4.selectedProperty());
        this.txt41.disableProperty().bind(this.btn4.selectedProperty().not());
        this.txt5.disableProperty().bind(this.btn5.selectedProperty());
        this.txt51.disableProperty().bind(this.btn5.selectedProperty().not());
        this.txt6.disableProperty().bind(this.btn6.selectedProperty());
        this.txt61.disableProperty().bind(this.btn6.selectedProperty().not());
        this.txt7.disableProperty().bind(this.btn7.selectedProperty());
        this.txt71.disableProperty().bind(this.btn7.selectedProperty().not());
        this.txtD.disableProperty().bind(this.btnD.selectedProperty());
        this.txtD1.disableProperty().bind(this.btnD.selectedProperty().not());
        this.txtM.disableProperty().bind(this.btnM.selectedProperty());
        this.txtM1.disableProperty().bind(this.btnM.selectedProperty().not());
        this.lvGenres.disableProperty().bind(this.btnG.selectedProperty());
        this.lvGenres1.disableProperty().bind(this.btnG.selectedProperty().not());
        this.lvActors.disableProperty().bind(this.btnA.selectedProperty());
        this.lvActors1.disableProperty().bind(this.btnA.selectedProperty().not());
        this.btn1.setSelected(true);
        this.btn3.setSelected(true);
        this.btn4.setSelected(true);
        this.btn5.setSelected(true);
        this.btn6.setSelected(true);
        this.btn7.setSelected(true);
        this.btnA.setSelected(true);
        this.btnG.setSelected(true);
        this.btnD.setSelected(true);
        this.btnM.setSelected(true);
        this.btnAll.setSelected(true);
        this.btnAll.setOnAction((e) -> {
            this.btn1.setSelected(this.btnAll.isSelected());
            this.btn3.setSelected(this.btnAll.isSelected());
            this.btn4.setSelected(this.btnAll.isSelected());
            this.btn5.setSelected(this.btnAll.isSelected());
            this.btn6.setSelected(this.btnAll.isSelected());
            this.btn7.setSelected(this.btnAll.isSelected());
            this.btnA.setSelected(this.btnAll.isSelected());
            this.btnG.setSelected(this.btnAll.isSelected());
            this.btnD.setSelected(this.btnAll.isSelected());
            this.btnM.setSelected(this.btnAll.isSelected());
        });
    }

    public boolean isUseTitle() {
        return this.btn1.isSelected();
    }

    public boolean isUseDate() {
        return this.btn3.isSelected();
    }

    public boolean isUseID() {
        return this.btn4.isSelected();
    }

    public boolean isUseStudio() {
        return this.btn5.isSelected();
    }

    public boolean isUseMaker() {
        return this.btnM.isSelected();
    }

    public boolean isUseSet() {
        return this.btn6.isSelected();
    }

    public boolean isUsePlot() {
        return this.btn7.isSelected();
    }

    public boolean isUseActors() {
        return this.btnA.isSelected();
    }

    public boolean isUseGenres() {
        return this.btnG.isSelected();
    }

    public boolean isUseDirector() {
        return this.btnD.isSelected();
    }

    public boolean isUseCover() {
        return this.btnUseCover.isSelected();
    }

    public boolean isUseExtraArt() {
        return this.btnUseExtra.isSelected();
    }

    public void setEditable(boolean canEdit) {
        this.txt1.setEditable(canEdit);
        this.txt3.setEditable(canEdit);

        this.txt4.setEditable(canEdit);
        this.txt5.setEditable(canEdit);
        this.txt6.setEditable(canEdit);
        this.txt7.setEditable(canEdit);
        this.txt11.setEditable(canEdit);
        this.txt31.setEditable(canEdit);
        /*this.txt31.setOnMouseClicked(e -> {
            if (!this.txt31.isEditable()) {
                this.txt31.hide();
            }
        });*/
        this.txt41.setEditable(canEdit);
        this.txt51.setEditable(canEdit);
        this.txt61.setEditable(canEdit);
        this.txt71.setEditable(canEdit);
        this.txtD.setEditable(canEdit);
        this.txtD1.setEditable(canEdit);
        this.txtM.setEditable(canEdit);
        this.txtM1.setEditable(canEdit);
    }

    public void setMovieOld(Map<String, SimpleStringProperty> customDataMap, ObservableList<ActorV2> actorObservableList, ObservableList<Genre> genreObservableList) {

        String pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        this.txt1.setText(customDataMap.get("Title").getValue());
        //this.txt2.setText(customDataMap.of("Year").getValue());
        if (customDataMap.get("Date").getValue() != null) {
            try {
                this.txt3.setText(LocalDate.parse(customDataMap.get("Date").getValue(), formatter).toString());
            } catch (DateTimeParseException e) {
                this.txt3.setText("");
            }
        }
        this.txt4.setText(customDataMap.get("ID").getValue());
        this.txt5.setText(customDataMap.get("Studio").getValue());
        this.txt6.setText(customDataMap.get("Set").getValue());
        this.txt7.setText(customDataMap.get("Plot").getValue());
        this.txtD.setText(customDataMap.get("Director").getValue());
        this.txtM.setText(customDataMap.get("Maker").getValue());

        this.lvActors.setItems(actorObservableList);
        this.lvGenres.setItems(genreObservableList);
    }

    public void setMovieNew(MovieV2 movie) {

        this.txtExtraImage.setText("Extra Image: " + movie.getImgExtras().length());
        this.btnExtraFanArt.setDisable(movie.getImgExtras().length() == 0);
        this.btnExtraFanArt.setOnAction((e) -> FXImageViewer.show(movie.getImgExtras()));

        this.btnCover.setDisable(movie.getImgBackCover().isEmpty());
        this.btnCover.setOnAction((e) -> {
            GUICommon.showDialog(movie.getImgBackCover().map(t->t.getThumbURL().toString()).getOrElse(""),
                    new ImageView(movie.getImgBackCover().map(FxThumb::getImage).getOrElse((Image)null)),
                    "Close", null, null);
        });

        this.btnUseCover.setDisable(this.btnCover.isDisable());
        this.btnUseExtra.setDisable(this.btnExtraFanArt.isDisable());
        this.btnUseCover.setSelected(!this.btnCover.isDisable());
        this.btnUseExtra.setSelected(!this.btnExtraFanArt.isDisable());

        String pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        this.txt11.setText(normalize(movie.getMovieTitle().getTitle()));
        //this.txt2.setText(movie.getYear().getYear());
        try {
            this.txt31.setText(LocalDate.parse(movie.getReleaseDates().map(ReleaseDate::getReleaseDate).getOrElse(""), formatter).toString());
        } catch (Exception e) {
            this.txt31.setText(null);
        }
        this.txt41.setText(movie.getMovieID().getId());
        this.txt51.setText(normalize(movie.getStudios().map(Studio::getStudio).getOrElse("")));
        this.txt61.setText(normalize(movie.getSets().map(Set::getSet).getOrElse("")));
        this.txt71.setText(normalize(movie.getPlots().map(Plot::getPlot).getOrElse("")));
        this.txtM1.setText(normalize(movie.getMovieMaker().getStudio()));
        if (movie.getDirectorList().size() > 0) {
            this.txtD1.setText(normalize(movie.getDirectorList().get(0).getName()));
        } else {
            this.txtD1.setText("");
        }

        if (Objects.equals(this.txt11.getText(), this.txt1.getText())) {
            this.btn1.setVisible(false);
        }
        if (Objects.equals(this.txt31.getText(), this.txt3.getText())) {
            this.btn3.setVisible(false);
        }

        if (this.txt4.getText() == null) {
            this.txt4.setText("");
        }
        this.autoSelection(this.txt4, this.txt41, this.btn4);
        this.autoSelection(this.txt5, this.txt51, this.btn5);
        this.autoSelection(this.txt6, this.txt61, this.btn6);
        this.autoSelection(this.txtD, this.txtD1, this.btnD);
        this.autoSelection(this.txtM, this.txtM1, this.btnM);

        if (Objects.equals(this.txt71.getText(), this.txt7.getText())) {
            this.btn7.setVisible(false);
        }
/*
        ObservableList<Actor> actorObservableList = FXCollections.observableArrayList();
        for (Actor actor : movie.getActors()) {
            if (actor.getThumb() != null) {
                Consumer<Image> callback = ((Image) ->
                {
                    actor.setImageIcon(Image);
                    Platform.runLater(() -> actorObservableList.add(0, actor));
                });

                system.out.println("Load Actor from URL >> " + actor.getName());
                actor.getThumb().getImage(callback);

            } else {
                actorObservableList.add(actor);
            }
        }
        if (actorObservableList.size() < this.lvActors.getItems().size()) {
            this.btnA.setSelected(false);
        }*/

        ObservableList<Genre> genreObservableList = FXCollections.observableArrayList(movie.getGenreList());

        this.btnA.setSelected(false);
        this.lvActors1.setItems(movie.getActorList());
        //this.lvActors1.setItems(actorObservableList);
        this.lvGenres1.setItems(genreObservableList);
    }

    private void autoSelection(JFXTextField t1, JFXTextField t2, JFXToggleButton b) {
        if (Objects.equals(t2.getText(), t1.getText())) {
            b.setVisible(false);
        } else if (t1.getText().length() > 0 && t2.getText().length() == 0) {
            b.setSelected(false);
        }
    }
}
