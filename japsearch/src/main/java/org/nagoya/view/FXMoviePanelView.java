package org.nagoya.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.nagoya.GUICommon;
import org.nagoya.controller.FXMoviePanelControl;
import org.nagoya.controller.siteparsingprofile.specific.ArzonParsingProfile;
import org.nagoya.controller.siteparsingprofile.specific.DmmParsingProfile;
import org.nagoya.controller.siteparsingprofile.specific.DugaParsingProfile;
import org.nagoya.controller.siteparsingprofile.specific.JavBusParsingProfile;
import org.nagoya.model.MovieV2;
import org.nagoya.model.dataitem.ActorV2;
import org.nagoya.model.dataitem.Genre;
import org.nagoya.system.FXMLController;
import org.nagoya.view.editor.FXSettingEditor;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class FXMoviePanelView extends FXMLController {

    private static final Rectangle2D boxBounds = new Rectangle2D(0, 0, 860, 660);
    private static final double ACTION_BOX_HGT = 0;
    @FXML
    public JFXButton btnSAll, btnSAllCustom;
    @FXML
    public JFXToggleButton btnSFast;
    @FXML
    public JFXButton btnSDmm, btnSArzon, btnSDuga, btnSJavBus;
    @FXML
    public JFXButton btnSaveMovie, btnExtraFanArt;
    private Rectangle clipRect;
    private Timeline timelineUp;
    private Timeline timelineDown;
    @FXML
    private Pane detailPane;
    private FXMovieDetailView movieDetailDisplay;
    private FXMoviePanelControl controller;
    private Runnable doChangeDetail;


    public FXMoviePanelView() {
    }

    public Image setImage(Image imageFront, Rectangle2D viewport, Image imageBack) {
        return this.movieDetailDisplay.setImage(imageFront, viewport, imageBack);
    }

    private void setAnimation() {
        /* Initial position setting for Top Pane*/
        this.clipRect = new Rectangle();
        this.clipRect.setWidth(ACTION_BOX_HGT);
        this.clipRect.setHeight(boxBounds.getHeight());
        this.clipRect.translateXProperty().set(boxBounds.getWidth() - ACTION_BOX_HGT);
        this.movieDetailDisplay.getPane().setClip(this.clipRect);
        this.movieDetailDisplay.getPane().translateXProperty().set(-(boxBounds.getWidth() - ACTION_BOX_HGT));

        /* Animation for bouncing effect. */
        final Timeline timelineDown1 = new Timeline();
        timelineDown1.setCycleCount(2);
        timelineDown1.setAutoReverse(true);
        final KeyValue kv1 = new KeyValue(this.clipRect.widthProperty(), (boxBounds.getWidth() - 15));
        final KeyValue kv2 = new KeyValue(this.clipRect.translateXProperty(), 15);
        final KeyValue kv3 = new KeyValue(this.movieDetailDisplay.getPane().translateXProperty(), -15);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(100), kv1, kv2, kv3);
        timelineDown1.getKeyFrames().add(kf1);

        /* Event handler to call bouncing effect after the scroll down is finished. */
        EventHandler<ActionEvent> onFinished = t -> timelineDown1.play();

        this.timelineDown = new Timeline();
        this.timelineUp = new Timeline();

        /* Animation for scroll down. */
        this.timelineDown.setCycleCount(1);
        this.timelineDown.setAutoReverse(true);
        final KeyValue kvDwn1 = new KeyValue(this.clipRect.widthProperty(), boxBounds.getWidth());
        final KeyValue kvDwn2 = new KeyValue(this.clipRect.translateXProperty(), 0);
        final KeyValue kvDwn3 = new KeyValue(this.movieDetailDisplay.getPane().translateXProperty(), 0);
        final KeyFrame kfDwn = new KeyFrame(Duration.millis(500), onFinished, kvDwn1, kvDwn2, kvDwn3);
        this.timelineDown.getKeyFrames().add(kfDwn);

        /* Animation for scroll up. */
        this.timelineUp.setCycleCount(1);
        this.timelineUp.setAutoReverse(true);
        final KeyValue kvUp1 = new KeyValue(this.clipRect.widthProperty(), ACTION_BOX_HGT);
        final KeyValue kvUp2 = new KeyValue(this.clipRect.translateXProperty(), boxBounds.getWidth() - ACTION_BOX_HGT);
        final KeyValue kvUp3 = new KeyValue(this.movieDetailDisplay.getPane().translateXProperty(), -(boxBounds.getWidth() - ACTION_BOX_HGT));
        final KeyFrame kfUp = new KeyFrame(Duration.millis(500), (event) -> {
            this.doChangeDetail.run();
            //this.timelineDown.play();
        }, kvUp1, kvUp2, kvUp3);
        this.timelineUp.getKeyFrames().add(kfUp);
    }

    private void setAnimationV() {
        /* Initial position setting for Top Pane*/
        this.clipRect = new Rectangle();
        this.clipRect.setWidth(boxBounds.getWidth());
        this.clipRect.setHeight(ACTION_BOX_HGT);
        this.clipRect.translateYProperty().set(boxBounds.getHeight() - ACTION_BOX_HGT);
        this.movieDetailDisplay.getPane().setClip(this.clipRect);
        this.movieDetailDisplay.getPane().translateYProperty().set(-(boxBounds.getHeight() - ACTION_BOX_HGT));

        /* Animation for bouncing effect. */
        final Timeline timelineDown1 = new Timeline();
        timelineDown1.setCycleCount(2);
        timelineDown1.setAutoReverse(true);
        final KeyValue kv1 = new KeyValue(this.clipRect.heightProperty(), (boxBounds.getHeight() - 15));
        final KeyValue kv2 = new KeyValue(this.clipRect.translateYProperty(), 15);
        final KeyValue kv3 = new KeyValue(this.movieDetailDisplay.getPane().translateYProperty(), -15);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(100), kv1, kv2, kv3);
        timelineDown1.getKeyFrames().add(kf1);

        /* Event handler to call bouncing effect after the scroll down is finished. */
        EventHandler<ActionEvent> onFinished = t -> timelineDown1.play();

        this.timelineDown = new Timeline();
        this.timelineUp = new Timeline();

        /* Animation for scroll down. */
        this.timelineDown.setCycleCount(1);
        this.timelineDown.setAutoReverse(true);
        final KeyValue kvDwn1 = new KeyValue(this.clipRect.heightProperty(), boxBounds.getHeight());
        final KeyValue kvDwn2 = new KeyValue(this.clipRect.translateYProperty(), 0);
        final KeyValue kvDwn3 = new KeyValue(this.movieDetailDisplay.getPane().translateYProperty(), 0);
        final KeyFrame kfDwn = new KeyFrame(Duration.millis(500), onFinished, kvDwn1, kvDwn2, kvDwn3);
        this.timelineDown.getKeyFrames().add(kfDwn);

        /* Animation for scroll up. */
        this.timelineUp.setCycleCount(1);
        this.timelineUp.setAutoReverse(true);
        final KeyValue kvUp1 = new KeyValue(this.clipRect.heightProperty(), ACTION_BOX_HGT);
        final KeyValue kvUp2 = new KeyValue(this.clipRect.translateYProperty(), boxBounds.getHeight() - ACTION_BOX_HGT);
        final KeyValue kvUp3 = new KeyValue(this.movieDetailDisplay.getPane().translateYProperty(), -(boxBounds.getHeight() - ACTION_BOX_HGT));
        final KeyFrame kfUp = new KeyFrame(Duration.millis(500), (event) -> {
            this.doChangeDetail.run();
            //this.timelineDown.play();
        }, kvUp1, kvUp2, kvUp3);
        this.timelineUp.getKeyFrames().add(kfUp);
    }

    public void runChangeEffect(MovieV2 movie) {
        this.btnExtraFanArt.setDisable(true);
        this.btnSaveMovie.setDisable(true);

        this.doChangeDetail = () -> {

            this.controller.updateMovieDetail(movie);

            if (movie != null) {
                this.timelineDown.play();
            }

            this.btnSAll.setDisable(false);
            // this.controller.getFXFileListControl().requestFocus();
        };

        this.timelineUp.play();
    }

    public void setController(FXMoviePanelControl controller) {
        this.controller = controller;
    }

    @FXML
    private void settingAction() {
        FXSettingEditor.showSettingEditor();
    }

    @FXML
    private void showExtraArtAction() {
        this.controller.displayExtraFanArt();
    }

    @FXML
    private void saveAction() {
        this.controller.doSaveAction();
    }

    @FXML
    private void scrapeAllCustom() {
        this.controller.doScrapeAll(true);
    }

    @FXML
    private void scrapeAll() {
        this.controller.doScrapeAll(false);
    }

    @FXML
    private void scrapeArzon() {
        this.controller.doScrapeDetail(ArzonParsingProfile.parserName());
    }

    @FXML
    private void scrapeDMM() {
        this.controller.doScrapeDetail(DmmParsingProfile.parserName());
    }

    @FXML
    private void scrapeDuga() {
        this.controller.doScrapeDetail(DugaParsingProfile.parserName());
    }

    @FXML
    private void scrapeJavBus() {
        this.controller.doScrapeDetail(JavBusParsingProfile.parserName());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.movieDetailDisplay = GUICommon.loadFXMLController(FXMovieDetailView.class);
        this.detailPane.getChildren().add(this.movieDetailDisplay.getPane());
        //this.detailPane.setVisible(false);
        this.setAnimation();

        this.btnSaveMovie.setDisable(true);
        this.btnExtraFanArt.setDisable(true);

        this.btnSAll.setDisable(true);
        this.btnSAllCustom.disableProperty().bind(this.btnSAll.disableProperty());

    }

    public void setData(Map<String, SimpleStringProperty> customDataMap, ObservableList<ActorV2> actorObservableList, ObservableList<Genre> genreObservableList) {
        this.movieDetailDisplay.bindData(customDataMap, actorObservableList, genreObservableList);
    }


    public void setActorList(ObservableList<ActorV2> actorObservableList) {
        this.movieDetailDisplay.setActorList(actorObservableList);
    }

    /*
        private String getButtonStyle() {
        return "-fx-background-color: \n" +
                "        #c3c4c4,\n" +
                "        linear-gradient(#d6d6d6 50%, white 100%),\n" +
                "        radial-gradient(center 50% -40%, radius 200%, #e6e6e6 45%, rgba(230,230,230,0) 50%);\n" +
                "    -fx-background-radius: 30;\n" +
                "    -fx-background-insets: 0,1,1;\n" +
                "    -fx-text-fill: black;\n" +
                "    -fx-font-size: 12;\n" +
                "    -fx-padding: 10 10 10 10;\n" +
                "    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 3, 0.0 , 0 , 1 );";
    }

  */
}
