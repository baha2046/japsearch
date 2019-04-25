package org.nagoya.view;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.nagoya.model.dataitem.FxThumb;

public class FXArtPanelView extends GridPane {

    private static final int maximumPosterSizeX = 400;
    private static final int maximumFanartSizeX = 400;
    private static final int maximumFanartSizeY = 280;
    private static final int maximumPosterSizeY = 460;

    private ImageView posterView;
    private ImageView fanartView;
    private FadeTransition ft1;
    private FadeTransition ft2;
    private WritableImage EmytyImage;
    private String movieID;

    public FXArtPanelView() {
        this.initJFXComponents();
    }

    public void setMovieID(String inMovieID) {
        this.movieID = inMovieID;
    }

    public void showImageWithoutPoster(@NotNull Image image, EventHandler<ActionEvent> onFinish) {
        this.fanartView.setImage(image);
        this.posterView.setImage(image);
        this.posterView.setViewport(FxThumb.getCoverCrop(image.getWidth(), image.getHeight(), this.movieID));
        this.ft1.play();
        this.ft2.setOnFinished(onFinish);
        this.ft2.play();
        //  system.out.println("SHOW IMAGE ");
    }

    public void showPoster(Image image) {
        this.posterView.setImage(image);
        this.posterView.setViewport(null);
        //GUICommon.debugMessage("SHOW showPoster ");
        this.ft1.play();
    }

    public void showCover(Image image) {
        this.fanartView.setImage(image);
        this.ft2.setOnFinished(null);
        this.ft2.play();
    }

    public void clear() {
        //system.out.println("CLEAR " );
        this.fanartView.setImage(this.EmytyImage);
        this.posterView.setImage(this.EmytyImage);
    }

    public void setOnCoverClick(EventHandler<? super MouseEvent> eventHandler) {
        this.fanartView.setOnMouseClicked(eventHandler);
    }

    public WritableImage getPosterImage() {
        return this.posterView.snapshot(new SnapshotParameters(), null);
    }

    private void initJFXComponents() {

        this.setPrefHeight(800);
        this.setPrefWidth(500);
        this.setAlignment(Pos.CENTER);
        this.setHgap(2);
        this.setVgap(0);
        this.setPadding(new Insets(0, 0, 0, 0));
        //setStyle("-fx-background-color: #336699;");

        Rectangle rec1 = new Rectangle(430.0, 1.0);
        rec1.setFill(Color.TRANSPARENT);

	  /*  Rectangle rec2 = new Rectangle(130.0, 25.0);
	    rec2.setFill(new LinearGradient(0,0,0,1, true, CycleMethod.NO_CYCLE,
	            new Stop[]{
	            new Stop(0,Color.web("#4977A3")),
	            new Stop(0.5, Color.web("#B0C6DA")),
	            new Stop(1,Color.web("#9CB6CF")),}));
	    rec2.setStroke(Color.web("#D0E6FA"));
	    rec2.setArcHeight(3.5);
	    rec2.setArcWidth(3.5);

	    Text txt1 = new Text("Poster");
	    txt1.setFont(Font.font("Arial", FontWeight.BOLD, 24));
	    txt1.setFill(Color.WHITE);
	    txt1.setStroke(Color.web("#7080A0"));

	    Text txt2 = new Text("Fanart");
	    txt2.setFont(Font.font("Arial", FontWeight.BOLD, 24));
	    txt2.setFill(Color.WHITE);
	    txt2.setStroke(Color.web("#7080A0"));

	    StackPane stack1 = new StackPane();
	    stack1.getChildren().addAll(rec1, txt1);
	    stack1.setAlignment(Pos.TOP_LEFT);     // Right-justify nodes in stack
	    StackPane.setMargin(txt1, new Insets(0, 10, 0, 28)); // Center "?"

	   // add(stack1, 1, 1);

	    StackPane stack2 = new StackPane();
	    stack2.getChildren().addAll(rec2, txt2);
	    stack2.setAlignment(Pos.TOP_LEFT);     // Right-justify nodes in stack
	    StackPane.setMargin(txt2, new Insets(0, 10, 0, 28)); // Center "?"

	   // add(stack2, 1, 4);*/


        this.EmytyImage = new WritableImage(10, maximumFanartSizeY);

        this.posterView = new ImageView(this.EmytyImage);
        this.fanartView = new ImageView(this.EmytyImage);

        this.posterView.setFitHeight(maximumPosterSizeY);
        this.posterView.setPreserveRatio(true);

        this.fanartView.setFitHeight(maximumFanartSizeY);
        this.fanartView.setPreserveRatio(true);

        this.ft1 = new FadeTransition();
        this.ft1.setNode(this.posterView);
        int fadeInTime = 700;
        this.ft1.setDuration(new Duration(fadeInTime));
        this.ft1.setFromValue(0.3);
        this.ft1.setToValue(1.0);
        this.ft1.setCycleCount(1);
        this.ft1.setAutoReverse(false);

        this.ft2 = new FadeTransition();
        this.ft2.setNode(this.fanartView);
        this.ft2.setDuration(new Duration(fadeInTime));
        this.ft2.setFromValue(0.3);
        this.ft2.setToValue(1.0);
        this.ft2.setCycleCount(1);
        this.ft2.setAutoReverse(false);

        TitledPane tp1 = new TitledPane();
        tp1.setText("Poster");
        tp1.setContent(this.posterView);

        TitledPane tp2 = new TitledPane();
        tp2.setText("Fan Art");
        tp2.setContent(this.fanartView);

        this.add(rec1, 0, 0);
        this.add(tp1, 0, 0);
        this.add(tp2, 0, 1);


        //root.getChildren().addAll(posterView, fanartView);
        //scene.setFill(Color.BLACK);

    }
}
