package org.nagoya.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import io.vavr.Tuple4;
import io.vavr.control.Option;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.nagoya.App;
import org.nagoya.GUICommon;
import org.nagoya.UtilCommon;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.preferences.RenameSettings;
import org.nagoya.system.DirectorySystem;
import org.nagoya.system.FXContextImp;
import org.nagoya.system.MovieLock;
import org.nagoya.system.Systems;
import org.nagoya.system.event.CustomEvent;
import org.nagoya.system.event.CustomEventType;
import org.nagoya.view.customcell.FileListRectCell;
import org.nagoya.view.dialog.FXImageViewer;
import org.nagoya.view.dialog.FXMoveMappingDialog;
import org.nagoya.view.dialog.FXRenameAllDialog;
import org.nagoya.view.editor.FXRenameFormatEditor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import static org.nagoya.system.DirectorySystem.EVENT_DIRECTORY_TREE_CHANGE;

public class FXFileListControl extends FXContextImp {

    public static final CustomEventType EVENT_LIST_SELECTED = new CustomEventType("EVENT_LIST_SELECTED");
    public static final CustomEventType EVENT_FOCUS = new CustomEventType("EVENT_FOCUS");

    private static FXFileListControl instance = null;

    @FXML
    private JFXButton btnMove, btnRenameAll;

    @FXML
    private JFXListView<DirectoryEntry> listView;

    public FXFileListControl() {
        //super(DirectoryEntry.class, Void.class);
    }

    public static FXFileListControl getInstance() {
        if (null == instance) {
            instance = GUICommon.loadFXMLController(FXFileListControl.class);
        }
        return instance;
    }

    public void requestFocus() {
        this.listView.requestFocus();
    }

   /* public void updateListAction(List<DirectoryEntry> entryList) {
        system.out.println("------------ ReadDirectoryAndUpdateList ------------");
        system.out.println(this.data.size());
        //system.out.println(this.loadCache.estimatedSize());

        if (this.data.size() != entryList.size()) {
            this.listView.scrollTo(0);
        }
        this.data.clear();
        this.data.addAll(entryList);
        //this.listView.scrollTo(0);
    }*/

    @FXML
    public void sortAction() {
        //Systems.getDirectorySystem().sortContain();

        //MangaParser.loadHImage();
    }


    @FXML
    public void upDirectoryAction() {
        int index = this.listView.getSelectionModel().getSelectedIndex();
        Systems.getDirectorySystem().upParent(index, GUICommon.getGuiSettings()::setLastUsedDirectory);
    }

    @FXML
    public void delAction() {
        if (this.listView.getSelectionModel().getSelectedItem() != null) {
            this.executeDelAction();
        }
    }

    @FXML
    public void reloadAction() {
        Systems.getDirectorySystem().reloadDirectory(this.listView.getSelectionModel().getSelectedIndex());
    }

    @FXML
    public void packDirectoryAction() {
        if (this.listView.getSelectionModel().getSelectedItem() != null) {
            DirectoryEntry directoryEntry = this.listView.getSelectionModel().getSelectedItem();
            this.executePackDirectoryAction(directoryEntry);
        }
    }

    @FXML
    public void renameAction() {
        if (this.listView.getSelectionModel().getSelectedItem() != null) {
            this.executeRenameAction(this.listView.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    public void renameAllAction() {
        FXRenameAllDialog.show();
    }

    @FXML
    public void browseAction() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        File dir = fileChooser.showDialog(App.getCurrentStage().getOrNull());
        if (dir != null) {
            Systems.getDirectorySystem().changePathTo(dir.toPath(), GUICommon.getGuiSettings()::setLastUsedDirectory, -1);
        }
    }

    @FXML
    public void moveAction() {
        if (this.listView.getSelectionModel().getSelectedItem() != null) {
            if (!this.listView.getSelectionModel().getSelectedItem().isDirectory() || !this.listView.getSelectionModel().getSelectedItem().hasNfo()) {
                return;
            }

            String company = this.listView.getSelectionModel().getSelectedItem().getMovieData().getMovieMaker().getStudio();

            if (company == null) {
                GUICommon.showDialog("Error", new Text("This movie has no maker info"), "Close", null, null);
                return;
            }

            FXMoveMappingDialog.show(null, this.listView.getSelectionModel().getSelectedItem(), this::executeMoveAction);
        }
    }

    private void doubleClickListAction() {
        DirectoryEntry select = this.listView.getSelectionModel().getSelectedItem();
        int index = this.listView.getSelectionModel().getSelectedIndex();

        if (select != null && MovieLock.getInstance().notInList(select.getFilePath())) {
            if (select.isDirectory()) {
//                Systems.getDirectorySystem().changePathTo(select.getFilePath(), GUICommon.getGuiSettings()::setLastUsedDirectory);
                if (select.isGalleryFolder()) {
                    FXImageViewer.show(select.getGalleryImages());
                } else {
                    Systems.getDirectorySystem().enterChildIndex(index, GUICommon.getGuiSettings()::setLastUsedDirectory);
                }
            } else {
                FXOpenFileAction.run(select);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------

    @Override
    public void registerListener() {
        this.registerListener(EVENT_FOCUS);
        this.registerListener(EVENT_DIRECTORY_TREE_CHANGE);

        Systems.getDirectorySystem().requestFullSync(this::updateListView);
    }

    @Override
    public void executeEvent(CustomEvent e) {
        if (e.getType().equals(EVENT_FOCUS)) {
            this.requestFocus();
        } else if (e.getType().equals(EVENT_DIRECTORY_TREE_CHANGE)) {
            this.updateListView((DirectorySystem.DirectoryChangeEvent) e.getObject());
        }
    }

    private void updateListView(@NotNull DirectorySystem.DirectoryChangeEvent directoryChangeEvent) {
        if (directoryChangeEvent.type == DirectorySystem.UpdateType.FULL) {
            this.listView.getItems().clear();
            this.listView.getItems().addAll(directoryChangeEvent.list.asJava());
            if (directoryChangeEvent.pos > -1) {
                this.listView.scrollTo(directoryChangeEvent.pos);
            }
        } else if (directoryChangeEvent.type == DirectorySystem.UpdateType.MODIFICATION) {
            int index = this.listView.getItems().indexOf(directoryChangeEvent.list.get(0));
            if (index > -1) {
                this.listView.getItems().set(index, directoryChangeEvent.list.get(1));
            }
        } else if (directoryChangeEvent.type == DirectorySystem.UpdateType.DELETION) {
            this.listView.getItems().remove(directoryChangeEvent.list.get(0));
        }
    }


    private void executeMoveAction(@NotNull DirectoryEntry directoryEntry, @NotNull Tuple4<Path, Path, Boolean, Boolean> finalPaths) {

        Path source = directoryEntry.getFilePath();

        boolean sameRoot = source.getRoot().equals(finalPaths._2().getRoot());

        File destFile = finalPaths._2().toFile();

        JFXTextField t1 = new JFXTextField("F - " + source.toString());
        JFXTextField t2 = new JFXTextField("T - " + finalPaths._2().toString());
        Text t3 = new Text("Target Maker Folder Exist = " + finalPaths._3());
        Text t4 = new Text();

        if (!sameRoot) {
            t4.setText("Warning : Source and Target is not on the same drive, move take times!");
        }

        t1.setEditable(false);
        t2.setEditable(false);

        VBox vBox = GUICommon.getVBox(15, t1, t2, new Separator(), t3, t4);
        vBox.setMinWidth(800);

        GUICommon.showDialog("Task :", vBox, "Cancel", "Okey", () -> {
            this.fireEvent(EVENT_LIST_SELECTED, null);
            //GUICommon.setLoading(true);
            Systems.useExecutors(() -> {
                try {
                    if (sameRoot) {
                        Path destP = destFile.toPath();
                        if (!Files.exists(destP.getParent())) {
                            Files.createDirectory(destP.getParent());
                        }
                        Files.move(source, destP, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        MovieLock.getInstance().addToList(source);
                        this.fireEvent(FXTaskBarControl.EVENT_ADD_TASK, "Moving File ...");
                        this.btnMove.setDisable(true);
                        this.btnRenameAll.setDisable(true);
                        FileUtils.moveDirectory(source.toFile(), destFile);
                        this.fireEvent(FXTaskBarControl.EVENT_REMOVE_TASK, "Moving File ...");
                        MovieLock.getInstance().removeFromList(source);
                        this.btnMove.setDisable(false);
                        this.btnRenameAll.setDisable(false);
                    }
                    //GUICommon.setLoading(false);

                    Platform.runLater(() ->
                    {
                        Systems.getDirectorySystem().removeEntry(directoryEntry);
                        directoryEntry.clearCache();

                        Option<DirectoryEntry> newParent = Systems.getDirectorySystem().getRelEntry(finalPaths._1);
                        if (newParent.isDefined()) {
                            newParent.get().resetChild();
                            if (Systems.getDirectorySystem().getCurrentPath().equals(newParent.get().getValue())) {
                                Systems.getDirectorySystem().reloadDirectory(-1);
                            }
                        }

                        GUICommon.showDialog("Copy Completed. Move to new directory ?", new Text(""), "No", "Yes", () ->
                                Systems.getDirectorySystem().changePathTo(finalPaths._1, null, -1)
                        );
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

    }

    private void executeRenameAction(@NotNull DirectoryEntry fileSelect) {

        TextField txtInput = new TextField(fileSelect.getFilePath().getFileName().toString());
        txtInput.setMinWidth(550);
        txtInput.setMaxWidth(550);

        HBox hBox = GUICommon.getHBox(15, txtInput);
        hBox.setMinWidth(750);
        hBox.setAlignment(Pos.CENTER);

        if (fileSelect.hasMovie() && fileSelect.hasNfo() && fileSelect.isDirectory()) {

            JFXButton btnEdit = GUICommon.getBorderButton("Setting", e -> {
                FXRenameFormatEditor.show(FXRenameFormatEditor.Type.DIRECTORY_NAME);
            });
            hBox.getChildren().add(btnEdit);

            JFXButton btnAuto = GUICommon.getBorderButton("Auto", e -> {
                txtInput.setText(RenameSettings.getSuitableDirectoryName(fileSelect.getMovieData()));
            });
            hBox.getChildren().add(btnAuto);
        }

        GUICommon.showDialog("Rename to :", hBox, "Cancel", "Okay", () -> {
            if (fileSelect.hasMovie() && fileSelect.hasNfo() && fileSelect.isDirectory()) {
                // fileSelect.getMovieData().getImgBackCover()
            }
            Path newPath = fileSelect.getValue().resolveSibling(txtInput.getText().replace(":", " "));
            Systems.getDirectorySystem().renameFile(fileSelect, newPath);
        });
    }

    private void executePackDirectoryAction(@NotNull DirectoryEntry directoryEntry) {
        if (!directoryEntry.isDirectory()) {
            String dirName = directoryEntry.getFilePath().getFileName().toString();
            dirName = dirName.replace("[Thz.la]", "");
            dirName = dirName.replace("[44x.me]", "");
            dirName = dirName.replace("[7sht.me]", "");
            dirName = dirName.replace("[99u.me]", "");
            dirName = dirName.replace("[88q.me]", "");
            dirName = dirName.replace("HD-", "");
            dirName = dirName.replace(".HD", "");
            int i = dirName.lastIndexOf(".");
            dirName = dirName.substring(0, i);

            JFXTextField textField = GUICommon.getTextField(dirName, 150);

            GUICommon.showDialog("Directory Naming", textField, "Cancel", "Ok", () -> {
                Path newDirectory = directoryEntry.getDirPath().resolve(textField.getText());
                try {
                    Files.createDirectory(newDirectory);
                    Path newPath = newDirectory.resolve(directoryEntry.getFilePath().getFileName());

                    if (UtilCommon.renameFile(directoryEntry.getFilePath(), newPath)) {
                        directoryEntry.clearCache();
                        Systems.getDirectorySystem().reloadDirectory(-1);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void executeDelAction() {

/*        for(int y=0; y<1000;y++) {

        }
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        GUICommon.debugMessage(nfo.map(p->p.getFileNameProperty().toString()) + "TIME : " + duration/1000);*/


        //cyclops.control.Option<String> oA = cyclops.control.Option.none();
        //cyclops.control.Option<String> oB = cyclops.control.Option.of("AAA");
        //cyclops.control.Option<String> oC = cyclops.control.Option.of("BBB");

/*        io.vavr.control.Option<String> oA = io.vavr.control.Option.none();
        io.vavr.control.Option<String> oB = io.vavr.control.Option.of("AAA");
        io.vavr.control.Option<String> oC = io.vavr.control.Option.of("BBB");

        Stream<io.vavr.control.Option<String>> sX = Stream.of(oA, oB, oC);
        Stream<String> sY = sX.flatMap(i -> i);

        GUICommon.debugMessage(sY.get(0) + " " + sY.get(1));*/

        //     this.listView.getSelectionModel().select(-1);
        GUICommon.debugMessage("DEL");
        //     MangaParser.load();


        int index = this.listView.getSelectionModel().getSelectedIndex();

        GUICommon.showDialog("Confirmation :", new Text("Are you sure you want to delete the file?"), "No", "Yes", () -> {
            Systems.getDirectorySystem().deleteFile(index);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.listView.setCellFactory((ListView<DirectoryEntry> l) -> new FileListRectCell());
        this.listView.getSelectionModel().selectedItemProperty().addListener((ov, old_val, new_val) ->
                this.fireEvent(EVENT_LIST_SELECTED, new_val));

        this.listView.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                GUICommon.debugMessage(event.getSource().toString());
                this.doubleClickListAction();
            }
        });

        this.listView.setItems(FXCollections.observableArrayList(DirectoryEntry.extractor()));
    }

    public void initializeListData(DirectorySystem directorySystem) {
        //this.data = FXCollections.observableArrayList(DirectoryEntry.extractor());
        /*this.listView.setItems(directorySystem.getDirectoryEntries());

        Set<Node> nodes = this.listView.lookupAll(".scroll-bar");
        for (Node node : nodes) {
            if (node instanceof ScrollBar) {
                ScrollBar scrollBar = (ScrollBar) node;
                if (scrollBar.getOrientation() == Orientation.VERTICAL) {
                    scrollBar.valueProperty().bindBidirectional(directorySystem.currentScrollbarPos);
                }
            }
        }*/


    }

    public void testTree(Path path) {
        // create LiveDirs to watch a directory

/*
        String EXTERNAL = "EXTERNAL";


        LiveDirs<String, Path> liveDirs = LiveDirs.getInstance(EXTERNAL);

        liveDirs.addTopLevelDirectory(path);

        // use LiveDirs as a TreeView model
        TreeView<Path> treeView = new TreeView<>(liveDirs.model().getRoot());
        treeView.setShowRoot(false);
        treeView.setCellFactory((TreeView<Path> l) -> new PathListTreeCell());


        // handle external changes
        liveDirs.model().modifications().subscribe(m -> {
            if (m.getInitiator() == EXTERNAL) {
                // handle external modification, e.g. reload the modified file
                //           reload(m.getPath());
            } else {
                // modification done by this application, no extra action needed
            }
        });

        GUICommon.showDialog("", treeView, "Done", null, () -> {
        });

*/

        Systems.getDirectorySystem().buildDirectoryTree();
    }
}
