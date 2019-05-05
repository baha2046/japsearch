package org.nagoya.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import io.vavr.control.Option;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nagoya.FileDownloaderUtilities;
import org.nagoya.GUICommon;
import org.nagoya.model.dataitem.FxThumb;
import org.nagoya.preferences.GuiSettings;
import org.nagoya.system.Systems;
import org.nagoya.view.customcell.SelectorDirectoryOnlyTreeItem;
import org.nagoya.view.customcell.SelectorPathListTreeCell;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class MangaParser {

    private static final String T_Dou = "Doujinshi";
    private static final String T_Man = "Manga";
    private static final String strUserAgent = "Hentai@Home 1.4.2";

    private static final io.vavr.collection.Vector<io.vavr.collection.Vector<String>> mapCookie = io.vavr.collection.Vector.of(
            io.vavr.collection.Vector.of("ipb_member_id", "346490"),
            io.vavr.collection.Vector.of("ipb_pass_hash", "da808be315bc4a9a35319117c1573591"),
            io.vavr.collection.Vector.of("igneous", "b556a8d1b"));

    private static final String strCookie =
            mapCookie.get(0).get(0) + "=" + mapCookie.get(0).get(1) + ";"
                    + mapCookie.get(1).get(0) + "=" + mapCookie.get(1).get(1) + ";"
                    + mapCookie.get(2).get(0) + "=" + mapCookie.get(2).get(1);

    public static void showWebView(String strUrl) {
        WebView webView = new WebView();
        URI uri = URI.create("https://exhentai.org/");

        Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
        headers.put("Set-Cookie", Arrays.asList(mapCookie.get(0).get(0) + "=" + mapCookie.get(0).get(1),
                mapCookie.get(1).get(0) + "=" + mapCookie.get(1).get(1),
                mapCookie.get(2).get(0) + "=" + mapCookie.get(2).get(1)));
        try {
            java.net.CookieHandler.getDefault().put(uri, headers);
        } catch (IOException e) {
            e.printStackTrace();
        }

        webView.setPrefSize(980, 600);
        webView.getEngine().load(strUrl);

        Pane pane = new Pane();
        pane.setMinSize(1000, 650);

        JFXTextField addressBar = GUICommon.getTextField(strUrl, 500);
        webView.getEngine().getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        addressBar.setText(webView.getEngine().getLocation());
                    }
                }
        );

        JFXButton btnGo = GUICommon.getBorderButton("  Go  ", e -> {
            webView.getEngine().load(addressBar.getText());
        });

        JFXButton btnDownload = GUICommon.getBorderButton("  Download  ", e -> {
            loadSet(addressBar.getText());
        });

        HBox hBox = GUICommon.getHBox(10, addressBar, btnGo, btnDownload);
        VBox vBox = GUICommon.getVBox(2, webView, hBox);

        pane.getChildren().addAll(vBox);

        GUICommon.showDialog("", pane, "Done", null, () -> {
        });
    }

    enum ChangeSource {
        INTERNAL, // indicates a change made by this application
        EXTERNAL, // indicates an external change
    }


    public static void buildTree() {
        //EditableStyledDocument editableStyledDocument;
        // create LiveDirs to watch a directory
      /*  try {
            LiveDirs<ChangeSource> liveDirs = new LiveDirs<>(ChangeSource.EXTERNAL);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static void loadHImage(String strUrl) {
        JFXListView<String> textArea = GUICommon.getTextArea(700, 500, true);

        JFXTextField txtInput = GUICommon.getTextField(strUrl, 560);

        JFXButton btnEdit = GUICommon.getOkButton("Download", e -> {
            textArea.getItems().clear();
            // loadSet(txtInput.getText(), Option.of(textArea.getItems()));
        });

        GUICommon.debugMessage(strCookie);

        HBox hBox = GUICommon.getHBox(15, txtInput, btnEdit);
        hBox.setMinWidth(700);
        hBox.setAlignment(Pos.CENTER);

        VBox vBox = GUICommon.getVBox(15, hBox, new Separator(), textArea);

        GUICommon.showDialog("e-hentai downloader :", vBox, "Done", null, () -> {
        });

    }

    public static Option<ObservableList<String>> createProgressDialog() {

        JFXListView<String> textArea = GUICommon.getTextArea(700, 300, true);

        VBox vBox = GUICommon.getVBox(15, textArea);

        GUICommon.showDialog("Progress :", textArea, "Close", null, () -> {
        });

        return Option.of(textArea.getItems());
    }

    private static String getTextWhenNotNull(Element element) {
        return getTextWhenNotNull(element, "unknown");
    }

    private static String getTextWhenNotNull(Element element, String useWhenNull) {
        if (element != null) {
            return element.text();
        } else {
            return useWhenNull;
        }
    }

    private static Option<Document> downloadDocument(String url) {
        try {
            return Option.of(Jsoup.connect(url).userAgent(strUserAgent)
                    .cookie(mapCookie.get(0).get(0), mapCookie.get(0).get(1))
                    .cookie(mapCookie.get(1).get(0), mapCookie.get(1).get(1))
                    .cookie(mapCookie.get(2).get(0), mapCookie.get(2).get(1))
                    .ignoreHttpErrors(true).timeout(20000).get());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Option.none();
    }

    private static Path getTargetPath(String strType) {
        if (Objects.equals(strType, T_Dou)) {
            return GuiSettings.getInstance().getDirectory(GuiSettings.Key.doujinshiDirectory);
        } else if (Objects.equals(strType, T_Man)) {
            return GuiSettings.getInstance().getDirectory(GuiSettings.Key.mangaDirectory);
        } else {
            return Paths.get("X:\\Download");
        }
    }

    private static Path getAuthorPath(String strType, Path pathTarget, String strAuthor) {
        if (Objects.equals(strType, T_Dou) && strAuthor.indexOf("(") > 2) {
            String strGroup = strAuthor.substring(0, strAuthor.indexOf("(") - 1) + "]";
            //GUICommon.debugMessage(strGroup);
            if (Files.isDirectory(pathTarget.resolve(strGroup))) {
                return pathTarget.resolve(strGroup);
            }
        }

        return pathTarget.resolve(strAuthor);
    }

    private static void loadSet(String fromURL) {
        Option<Document> document = downloadDocument(fromURL + "?nw=always");

        if (document.isEmpty()) {
            return;
        }

        //GUICommon.debugMessage(document.toString());

        String strWorkTitle = getTextWhenNotNull(document.get().select("#gj").first());
        strWorkTitle = strWorkTitle.replace("/", " ");
        strWorkTitle = strWorkTitle.replace(" [DL版]", "");

        Element elementCover = document.get().select("div[id=gd1] div").first();
        String strCover = elementCover.attr("style");
        strCover = strCover.substring(strCover.indexOf("(") + 1, strCover.indexOf(")"));

        //GUICommon.debugMessage(strCover);

        String strWorkType = getTextWhenNotNull(document.get().select("div[id=gdc] div").first(), "Misc");
        String strWorkAuthor = "[unknown]";

        int i1 = strWorkTitle.indexOf("[");
        int i2 = strWorkTitle.indexOf("]");
        if (i1 > -1 && i2 > -1) {
            strWorkAuthor = strWorkTitle.substring(i1, i2 + 1);
        }

        GUICommon.debugMessage(strWorkTitle + " " + strWorkAuthor);

        Path pathTypeDir = getTargetPath(strWorkType);
        Path pathAuthorDir = getAuthorPath(strWorkType, pathTypeDir, strWorkAuthor);

        JFXTextField txtType = GUICommon.getTextField(strWorkType, 550);
        JFXTextField txtAuthor = GUICommon.getTextField(strWorkAuthor, 550);
        JFXTextField txtTitle = GUICommon.getTextField(strWorkTitle, 550);

        txtType.setDisable(true);

        JFXTextField txtAuthorDirectory = GUICommon.getTextField(pathAuthorDir.toString(), 550);

        HBox hBox = GUICommon.getHBox(15, txtAuthorDirectory);

        if (Files.notExists(pathAuthorDir)) {
            txtAuthorDirectory.setMinWidth(450);
            txtAuthorDirectory.setMaxWidth(450);
            JFXButton btnAdd = GUICommon.getBorderButton("Create", e -> {
            });
            btnAdd.setOnAction(e -> {
                btnAdd.setVisible(false);
                try {
                    Files.createDirectory(Paths.get(txtAuthorDirectory.getText()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                txtAuthorDirectory.setMinWidth(550);
                txtAuthorDirectory.setMaxWidth(550);
            });
            hBox.getChildren().add(btnAdd);
        }

        TreeView<Path> treeView = new TreeView<>();
        treeView.setRoot(SelectorDirectoryOnlyTreeItem.createNode(Files.exists(pathAuthorDir) ? pathAuthorDir : pathAuthorDir.getParent()));
        treeView.setShowRoot(false);
        treeView.setCellFactory((TreeView<Path> l) ->
                SelectorPathListTreeCell.createCell("Use Folder", path -> txtAuthorDirectory.setText(path.toString()))
        );

        VBox vBox = GUICommon.getVBox(15, txtType, new Separator(), txtAuthor, txtTitle, new Separator(), hBox, treeView);

        var fxThumb = FxThumb.of(strCover);
        ImageView imgCover = new ImageView();
        imgCover.setPreserveRatio(true);
        imgCover.setFitWidth(250.0);
        fxThumb.peek(v -> v.setCookie(strCookie)).peek(v -> v.getImage(imgCover::setImage));

        HBox hBox2 = GUICommon.getHBox(15, imgCover, vBox);

        Systems.useExecutors(() -> {
            treeView.getRoot().setExpanded(true);
        });

        GUICommon.showDialog("Download Data :", hBox2, "Cancel", "Confirm", () -> {

            var observableList = createProgressDialog();

            Systems.useExecutors(() -> {

                GUICommon.writeToObList(txtTitle.getText(), observableList);

                Element firstImageURL = document.get().select("div[id=gdt] div a").first();

                Path outImageDirectory;

                if (Files.notExists(Paths.get(txtAuthorDirectory.getText()))) {
                    outImageDirectory = pathTypeDir.resolve(txtTitle.getText());
                } else {
                    outImageDirectory = Paths.get(txtAuthorDirectory.getText()).resolve(txtTitle.getText());
                }

                GUICommon.checkAndCreateDir(outImageDirectory);

                DecimalFormat df = new DecimalFormat("000");

                GUICommon.writeToObList("", observableList);

                loadSingleImage(firstImageURL.attr("href"), outImageDirectory, df, observableList);

                GUICommon.writeToObList("Done", observableList);
            });
        });

        // https://exhentai.org/g/5222/13f214a9e1/
    }

    private static void loadSingleImage(String fromURL, Path outDir, DecimalFormat df, Option<ObservableList<String>> observableList) {
        Option<Document> document = downloadDocument(fromURL);

        if (document.isEmpty()) {
            return;
        }
        //GUICommon.debugMessage(document.toString());

        Element controlBar = document.get().select("div.sn").first();
        Element count = controlBar.select("div div").first();

        Integer currentNum = new Integer(count.select("span").first().text());
        Integer totalNum = new Integer(count.select("span").last().text());

        String nextImage = controlBar.select("#next").attr("href");
        String imageURL = document.get().select("div[id=i3] img").first().attr("src");


        GUICommon.writeToObListWithoutNewLine("Downloading " + currentNum + " / " + totalNum + " | " + imageURL, observableList);

        //GUICommon.debugMessage(currentNum + " / " + totalNum);
        //GUICommon.debugMessage(imageURL.attr("src"));

        Systems.useExecutors(() -> {
            try {
                if (currentNum == 1) {
                    FileDownloaderUtilities.writeURLToFile(new URL(imageURL), new File(outDir.toFile(), "folder.jpg"), strUserAgent, strCookie);
                }
                FileDownloaderUtilities.writeURLToFile(new URL(imageURL), new File(outDir.toFile(), df.format(currentNum) + ".jpg"), strUserAgent, strCookie);
            } catch (Exception e) {
                try {
                    FileDownloaderUtilities.writeURLToFile(new URL(imageURL), new File(outDir.toFile(), df.format(currentNum) + ".jpg"), strUserAgent, strCookie);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        if (currentNum < totalNum) {
            loadSingleImage(nextImage, outDir, df, observableList);
        }
    }

    public static void load() {
        // http://www.dmm.co.jp/mono/dvd/-/list/=/article=maker/id=6329/
        String mid = "4818";

/*
        Systems.useExecutors(() -> {
            Document document = SiteParsingProfile.downloadDocumentFromURLString("http://www.dmm.co.jp/mono/dvd/-/list/=/article=label/id=" + mid + "/");
            Element nextPageLink;

            ArrayList<String> pagesVisited = new ArrayList<>();
            while (true) {

                nextPageLink = document.select("div.list-capt div.list-boxcaptside.list-boxpagenation ul li:not(.terminal) a").last();
                String currentPageURL = document.baseUri();
                String nextPageURL = "";
                if (nextPageLink != null) {
                    nextPageURL = nextPageLink.attr("abs:href");
                }
                pagesVisited.add(currentPageURL);

                Elements dvdLinks = document.select("p.tmb a[href*=/mono/dvd/]");

                for (Element dvdLink : dvdLinks) {
                    String currentLink = dvdLink.attr("abs:href");
                    if (!currentLink.matches(".*dod/.*")) {
                        VirtualEntry virtualEntry = new VirtualEntry();
                        virtualEntry.set(currentLink);

                        var currentList = Systems.getDirectorySystem().getDirectoryEntries();
                        boolean isAvailable = false;
                        for (DirectoryEntry d : currentList) {
                            if (d.hasNfo()) {
                                if (d.getMovieData().getId().getId().equals(virtualEntry.getMovieData().getId().getId())) {
                                    isAvailable = true;
                                    break;
                                }
                            }
                        }

                        if (!isAvailable) {
                            Platform.runLater(() -> {
                                Systems.getDirectorySystem().getDirectoryEntries().add(virtualEntry);
                                Systems.getDirectorySystem().sortContain();
                            });
                        }
                    }
                }

                if (nextPageLink != null && !pagesVisited.contains(nextPageURL)) {
                    document = SiteParsingProfile.downloadDocumentFromURLString(nextPageURL);
                } else {
                    break;
                }

            }
        });

        */
    }
}
