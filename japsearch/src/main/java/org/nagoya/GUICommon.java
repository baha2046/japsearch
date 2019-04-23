package org.nagoya;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import cyclops.control.Option;
import cyclops.control.Try;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.vavr.collection.Stream;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MaskerPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nagoya.preferences.GeneralSettings;
import org.nagoya.preferences.GuiSettings;
import org.nagoya.system.FXMLController;
import org.nagoya.system.Systems;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Common utility methods used in the various GUI classes. Methods should be static.
 */

public class GUICommon {

    private static MaskerPane progressPane = null;

    public static final Boolean DEBUG_MODE = Boolean.getBoolean("debug");

    private static final GuiSettings guiSettings = GuiSettings.getInstance();
    private static final GeneralSettings preferences = GeneralSettings.getInstance();

    public static MaskerPane getProgressPane() {
        if(null == progressPane) progressPane = new MaskerPane();
        return progressPane;
    }

    public static void setLoading(boolean loading) {
        getProgressPane().setVisible(loading);
    }

    public static URL pathToUrl(@NotNull Path path) {
        URL url = null;
        try {
            url = path.toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }



    public static Image getProgramIcon() {
        //initialize the icons used in the program
        URL programIconURL = GUICommon.class.getResource("/res/AppIcon.png");

        //Used for icon in the title bar
        Image programIcon = null;
        try {
            programIcon = ImageIO.read(programIconURL);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return programIcon;
    }

    private static <T extends FXMLController> Option<Node> loadFXML(@NotNull FXMLLoader fxmlLoader) {
        return Try.withCatch(fxmlLoader::load, IOException.class).map(n -> (Node) n).toOption();
    }

    private static <T extends FXMLController> T getController(@NotNull FXMLLoader fxmlLoader) {
        Option<Node> nodeOption = loadFXML(fxmlLoader);
        Option<T> control = Option.ofNullable(fxmlLoader.getController());
        return nodeOption.flatMap(node -> control.peek(c -> c.setPane(node))).orElse(null);
    }

    @Nullable
    public static <T extends FXMLController> T loadFXMLController(String fxml) {
        FXMLLoader fxmlLoader = new FXMLLoader(GUICommon.class.getResource(fxml));
        return getController(fxmlLoader);
    }

    @Nullable
    public static <T extends FXMLController> T loadFXMLController(@NotNull Class<T> tClass) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(tClass.getResource("/fxml/" + tClass.getSimpleName().replace("Control", "") + ".fxml"));
        return getController(fxmlLoader);
    }
        /*fxmlLoader.setResources();
        try {
            Node node = fxmlLoader.load();
            T fxmlController = fxmlLoader.getController();
            fxmlController.setPane(node);
            return fxmlController;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }*/

    public static <T> void loadFXMLRoot(String fxml, T tClass, Node root) {
        FXMLLoader fxmlLoader = new FXMLLoader(GUICommon.class.getResource(fxml));
        loadFXMLRoot(fxmlLoader, tClass, root);
    }

    public static <T extends Node> void loadFXMLRoot(@NotNull T tClass) {
        loadFXMLRoot(tClass, tClass);
    }

    public static <T> void loadFXMLRoot(@NotNull T tClass, Node root) {
        FXMLLoader fxmlLoader = new FXMLLoader(tClass.getClass().getResource("/fxml/" + tClass.getClass().getSimpleName() + ".fxml"));
        loadFXMLRoot(fxmlLoader, tClass, root);
    }

    private static <T> void loadFXMLRoot(@NotNull FXMLLoader fxmlLoader, T tClass, Node root) {
        fxmlLoader.setRoot(root);
        fxmlLoader.setController(tClass);
        loadFXML(fxmlLoader);
    }

    public static Path checkAndCreateDir(Path dir)
    {
        if(Files.isRegularFile(dir))
        {
            try {
                Files.delete(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(Files.notExists(dir)) {
            try {
                Files.createDirectory(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dir;
    }


    public static Stream<File> getDirectoryPathStream(Path path) {
        File[] list = io.vavr.control.Try.of(() -> path.toFile().listFiles()).toOption().getOrElse(new File[0]);
        return Stream.of(list);
    }

    public static JFXDialog getAvailableDialog() {
        return Systems.getDialogPool().taketDialog();
    }


    public static void showDialog(String heading, Node body, String strBtnCancel, String strBtnOkay, Runnable runnable) {
        Systems.getDialogPool().showDialog(heading, body, strBtnCancel, strBtnOkay, runnable);
    }

    public static void showDialog(JFXDialog useDialog, String heading, Node body, String strBtnCancel, String strBtnOkay, Runnable runnable) {
        Systems.getDialogPool().showDialog(useDialog, heading, body, strBtnCancel, strBtnOkay, runnable);
    }

    public static void loadImageFromURL(URL url, double w, double h, Consumer<javafx.scene.image.Image> callWhenFinish) {
        Systems.useExecutors(() -> {
            javafx.scene.image.Image image = loadImageFromURL(url, w, h);
            Platform.runLater(() -> callWhenFinish.accept(image));
        });
    }

    public static javafx.scene.image.Image loadImageFromFile(@NotNull Path path, double w, double h) {
        return Try.withResources(() -> new FileInputStream(path.toFile()),
                stream -> new javafx.scene.image.Image(stream, w, h, true, true),
                IOException.class)
                .onFail((e) -> GUICommon.debugMessage("loadImageFromFile >> File not found : " + path.toString()))
                .get()
                .orElse(null);
    }

    public static javafx.scene.image.Image loadImageFromURL(@NotNull URL url, double w, double h) {
        //GUICommon.debugMessage("GUICommon loadImageFromURL >> " + url.toString());
        return Try.withCatch(url::openConnection)
                .peek(conn -> conn.setRequestProperty("User-Agent", "Wget/1.13.4 (linux-gnu)"))
                .peek(conn -> conn.setRequestProperty("Referer", customReferrer(url, null)))
                .flatMap(conn -> Try.withResources(conn::getInputStream,
                        stream -> new javafx.scene.image.Image(stream, w, h, true, true),
                        IOException.class))
                .onFail((e) -> GUICommon.debugMessage("loadImageFromURL >> Cannot of image from : " + url.toString()))
                .get()
                .orElse(null);
    }

    public static void writeToObList(String text, io.vavr.control.Option<ObservableList<String>> observableListOption) {
        if (!Thread.currentThread().getName().equals("JavaFX Application Thread")) {
            Platform.runLater(() -> observableListOption.peek(t -> t.add(text)));
        } else {
            observableListOption.peek(t -> t.add(text));
        }
    }

    public static String customReferrer(URL url, URL refer) {
        String referrerString = "";

        if (refer != null) {
            referrerString = refer.toString();
        }

        if (url != null) {
            String urlString = url.toString();

            if (urlString.contains(".arzon.jp")) {
                referrerString = "https://www.arzon.jp/item_140797.html";
            }
        }

        return referrerString;
    }

    public static void debugMessage(String string) {
        if (DEBUG_MODE) {
            System.out.println(string);
        }
    }

    public static void debugMessage(@NotNull Supplier<String> string) {
        if (DEBUG_MODE) {
            System.out.println(string.get());
        }
    }


    public static JFXButton getButton(String text, EventHandler<ActionEvent> eventHandler) {
        JFXButton jfxButton = new JFXButton(text);
        jfxButton.setOnAction(eventHandler);
        return jfxButton;
    }

    public static JFXButton getBorderButton(String text, EventHandler<ActionEvent> eventHandler) {
        JFXButton jfxButton = getButton(text, eventHandler);
        jfxButton.setStyle(" -fx-border-color: #AAAAAA; -fx-border-insets: 1; -fx-border-radius: 4;");
        return jfxButton;
    }

    public static JFXButton getOkButton(String text, EventHandler<ActionEvent> eventHandler) {
        JFXButton jfxButton = getBorderButton(text, eventHandler);
        jfxButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE));
        return jfxButton;
    }

    public static VBox getVBox(double spaceing, Node... elements) {
        VBox vBox = new VBox(spaceing);
        vBox.getChildren().addAll(elements);
        return vBox;
    }

    public static HBox getHBox(double spaceing, Node... elements) {
        HBox hBox = new HBox(spaceing);
        hBox.getChildren().addAll(elements);
        return hBox;
    }

    public static JFXListView<String> getTextArea(double width, double height, boolean autoScroll)
    {
        JFXListView<String> textArea = new JFXListView<>();
        textArea.setMinHeight(height);
        textArea.setMaxHeight(height);
        textArea.setMinWidth(width);
        textArea.setMaxWidth(width);
        if(autoScroll) textArea.getItems().addListener((ListChangeListener<String>) c -> textArea.scrollTo(c.getList().size()-1));
        return textArea;
    }

    public static JFXTextField getTextField(String text, double width)
    {
        JFXTextField textField = new JFXTextField(text);
        if(width > 0)
        {
            textField.setMinWidth(width);
            textField.setMaxWidth(width);
        }
        return textField;
    }

    public static GuiSettings getGuiSettings() {
        return GUICommon.guiSettings;
    }

    public static GeneralSettings getPreferences() {
        return GUICommon.preferences;
    }
}
