package org.nagoya;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vavr.control.Option;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.MaskerPane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nagoya.controller.*;
import org.nagoya.controller.siteparsingprofile.specific.ArzonCSSQuery;
import org.nagoya.controller.siteparsingprofile.specific.DmmCSSQuery;
import org.nagoya.controller.siteparsingprofile.specific.DugaCSSQuery;
import org.nagoya.controller.siteparsingprofile.specific.JavBusCSSQuery;
import org.nagoya.system.Systems;
import org.nagoya.system.cache.IconCache;
import org.nagoya.system.cache.MovieV2Cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    private static Stage currentStage = null;

    private static BorderPane mainScreen;

    public static BorderPane getMainScreen() {
        return mainScreen;
    }

    @Override
    public void start(Stage stage) throws IOException {

        stage.setTitle("JAV DATA SEARCHER");

        currentStage = stage;
        //scene = new Scene(loadFXML("primary"));

        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);

        stage.setScene(this.initLoadingScene());
        stage.show();

        Systems.useExecutors(() ->
        {
            this.loadSettings();

            IconCache.setIconProvider(GUICommon.getGuiSettings().getUseContentBasedTypeIcons() ? IconCache.IconProviderType.CONTENT : IconCache.IconProviderType.SYSTEM);

            scene = this.initMainScene();

            Platform.runLater(() ->
            {
                stage.setScene(scene);
                stage.centerOnScreen();
                Systems.getDirectorySystem().changePathTo(GUICommon.getGuiSettings().getLastUsedDirectory(), null, -1);

                //FXFileListControl.getInstance().testTree(GUICommon.getGuiSettings().getLastUsedDirectory());
                //Systems.getDirectorySystem().changePathTo(GUICommon.getGuiSettings().getDirectory(GuiSettings.Key.avDirectory), null);
                FXFileListControl.getInstance().initializeListData(Systems.getDirectorySystem());
            });
            //primaryStage.show();
        });

        //this makes all stages close and the app exit when the main stage is closed
        stage.setOnCloseRequest(e -> {
            MovieV2Cache.getInstance().saveCacheFile();
            Systems.shutdown();
            Platform.exit();
        });
    }

    private void loadSettings() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT);
        Gson gson = gsonBuilder.create();

        Path path = Paths.get("");

        if (Files.notExists(path.resolve("dmmQuery.ini"))) {
            UtilCommon.saveStringToFile(path.resolve("dmmQuery.ini"), gson.toJson(new DmmCSSQuery()));
        } else {
            DmmCSSQuery dummy = gson.fromJson(UtilCommon.readStringFromFile(path.resolve("dmmQuery.ini")), DmmCSSQuery.class);
            GUICommon.debugMessage(">> Loaded dmmQuery.ini");
        }

        if (Files.notExists(path.resolve("arzonQuery.ini"))) {
            UtilCommon.saveStringToFile(path.resolve("arzonQuery.ini"), gson.toJson(new ArzonCSSQuery()));
        } else {
            ArzonCSSQuery dummy = gson.fromJson(UtilCommon.readStringFromFile(path.resolve("arzonQuery.ini")), ArzonCSSQuery.class);
            GUICommon.debugMessage(">> Loaded arzonQuery.ini");
        }

        if (Files.notExists(path.resolve("dugaQuery.ini"))) {
            UtilCommon.saveStringToFile(path.resolve("dugaQuery.ini"), gson.toJson(new DugaCSSQuery()));
        } else {
            DugaCSSQuery dummy = gson.fromJson(UtilCommon.readStringFromFile(path.resolve("dugaQuery.ini")), DugaCSSQuery.class);
            GUICommon.debugMessage(">> Loaded dugaQuery.ini");
        }

        if (Files.notExists(path.resolve("javbusQuery.ini"))) {
            UtilCommon.saveStringToFile(path.resolve("javbusQuery.ini"), gson.toJson(new JavBusCSSQuery()));
        } else {
            JavBusCSSQuery dummy = gson.fromJson(UtilCommon.readStringFromFile(path.resolve("javbusQuery.ini")), JavBusCSSQuery.class);
            GUICommon.debugMessage(">> Loaded javbusQuery.ini");
        }
    }

    @Contract(pure = true)
    public static Option<Stage> getCurrentStage() {
        return Option.of(currentStage);
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

    @NotNull
    @Contract(" -> new")
    private Scene initMainScene() {

        mainScreen = new BorderPane();
        mainScreen.setStyle("-fx-background-color: #336699;");
        mainScreen.setBorder(Border.EMPTY);

        mainScreen.setCenter(FXMoviePanelControl.getInstance().getPane());
        mainScreen.setLeft(FXFileListControl.getInstance().getPane());
        mainScreen.setRight(FXArtPanelControl.getInstance().getPane());
        mainScreen.setBottom(FXTaskBarControl.getInstance().getPane());

        FXCoreController.getInstance().addContext(FXMoviePanelControl.getInstance());
        FXCoreController.getInstance().addContext(FXFileListControl.getInstance());
        //FXCoreController.getInstance().addContext(FXArtPanelControl.getInstance());
        FXCoreController.getInstance().addContext(FXTaskBarControl.getInstance());

        FXCoreController.getInstance().addContext(FXMangaPanelControl.getInstance());

        //  FXMangaList.getInstance().loadWeb();

        StackPane sPane = new StackPane();
        sPane.setAlignment(Pos.TOP_LEFT);

        MaskerPane progressPane = GUICommon.getProgressPane();
        sPane.getChildren().setAll(mainScreen, progressPane);
        progressPane.setVisible(false);

        Systems.getDialogPool().initialize(5, sPane);

        return new Scene(sPane, 1400, 800);
    }


}