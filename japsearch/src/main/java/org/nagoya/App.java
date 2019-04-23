package org.nagoya;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.MaskerPane;
import org.nagoya.system.Systems;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static MaskerPane progressPane;

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        stage.setTitle("JAV DATA SEARCHER");

        //scene = new Scene(loadFXML("primary"));

        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);

        stage.setScene(initLoadingScene());
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    private Scene initLoadingScene() {

        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setStyle("-fx-background-color:#336699; -fx-opacity:1;");
        pane.setPrefWidth(730);
        pane.setPrefHeight(430);//screenHeight);
        pane.add(new Text("Loading..."), 0, 0);

        Group root = new Group(pane);

        return new Scene(root, 700, 400);
    }

    private Scene initMainScene()
    {

        BorderPane bPane = new BorderPane();
        bPane.setStyle("-fx-background-color: #336699;");
        bPane.setBorder(Border.EMPTY);

     /*   bPane.setCenter(FXMoviePanelControl.getInstance().getPane());
        bPane.setLeft(FXFileListControl.getInstance().getPane());
        bPane.setRight(FXArtPanelControl.getInstance().getPane());
        bPane.setBottom(FXTaskBarControl.getInstance().getPane());

        FXCoreController.getInstance().addContext(FXMoviePanelControl.getInstance());
        FXCoreController.getInstance().addContext(FXFileListControl.getInstance());
        FXCoreController.getInstance().addContext(FXArtPanelControl.getInstance());
        FXCoreController.getInstance().addContext(FXTaskBarControl.getInstance());

        FXMangaList.getInstance().loadWeb();
*/
        StackPane sPane = new StackPane();
        sPane.setAlignment(Pos.TOP_LEFT);

        progressPane = new MaskerPane();
        sPane.getChildren().setAll(bPane, progressPane);
        progressPane.setVisible(false);

        Systems.getDialogPool().initialize(5, sPane);

        return new Scene(sPane, 1400, 850);
    }

}