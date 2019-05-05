package org.nagoya.view.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import io.vavr.Tuple;
import io.vavr.Tuple4;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.model.MovieV2;
import org.nagoya.model.PathCell;
import org.nagoya.preferences.GuiSettings;
import org.nagoya.preferences.RenameSettings;
import org.nagoya.view.customcell.SelectorDirectoryOnlyTreeItem;
import org.nagoya.view.customcell.SelectorPathListTreeCell;
import org.nagoya.view.editor.FXPathMappingEditor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FXMoveMappingDialog {

    public static String doRename(String maker) {
        return RenameSettings.getInstance().renameCompany(maker);
    }

    public static void show(ObservableList<Path> paths, @NotNull DirectoryEntry directoryEntry,
                            BiConsumer<DirectoryEntry, Tuple4> call) {

        // GridView<PathCell> gridView = new GridView<>();

        MovieV2 movie = directoryEntry.getMovieData();
        Path dest = GuiSettings.getInstance().getDirectory(GuiSettings.Key.avDirectory);
        //String makerString = doRename(movie.getMovieMaker().getStudio());

        TreeView<Path> treeView = new TreeView<>();
        treeView.setRoot(SelectorDirectoryOnlyTreeItem.createNode(dest));
        treeView.setShowRoot(false);
        treeView.getRoot().setExpanded(true);
        ObservableList<TreeItem<Path>> itemObservableList = treeView.getRoot().getChildren();

        JFXTextField txtO = GUICommon.getTextField(movie.getMovieMaker().getStudio(), 400);
        JFXTextField txtT = GUICommon.getTextField("", 400);

        Runnable runSelectDir = () -> {
            String makerString = doRename(movie.getMovieMaker().getStudio()).replace("/", "／");
            txtT.setText(makerString);

            for (int x = 0; x < itemObservableList.size(); x++) {
                TreeItem<Path> pathTreeItem = itemObservableList.get(x);

                if (pathTreeItem.getValue().getFileName().toString().equals("(" + makerString + ")")) {
                    treeView.getSelectionModel().select(x);
                    treeView.scrollTo(x);
                    break;
                }
            }
        };

        Consumer<Path> addNewMapping = path -> {
            if (path != null) {
                String strMaker = path.getFileName().toString();
                if (strMaker.charAt(0) == '(') {
                    strMaker = strMaker.substring(1, strMaker.length() - 1);
                }

                final String newMapping = movie.getMovieMaker().getStudio() + " >> " + strMaker;
                GUICommon.showDialog("Confirm :", new Text("Set [  " + newMapping + "  ] as new mapping ?"), "Cancel",
                        "Okay", () -> {
                            RenameSettings.getInstance().updateRenameMapping(newMapping.replace(" >> ", "|"));
                            RenameSettings.getInstance().writeSetting();
                            runSelectDir.run();
                        });
            }
        };

        treeView.setCellFactory((TreeView<Path> l) ->
                SelectorPathListTreeCell.createCell("Add New Mapping", addNewMapping)
        );

        runSelectDir.run();


        //ReturnPaths returnPaths = new ReturnPaths(directoryEntry);

    /*    vectorFuture.peek(pathCells -> {
            String makerStringAfterMapping = RenameSettings.getInstance().renameCompany(movie.getMovieMaker().getStudio())
                    .replace("/", "／");
            Platform.runLater(() -> {
                stringObservableList.addAll(pathCells.toJavaList());
                out2.setText(updateMappingDisplay(stringObservableList, makerStringAfterMapping));
                returnPaths.setFinalPaths(getTargetPath(directoryEntry.getFilePath(), dest, makerStringAfterMapping));
                out3.setText("Target : " + returnPaths.getFinalPaths()._2().toString());
            });
        });

        //JFXTextField out1 = new JFXTextField();
        //out1.setText("Target: " + findPath.map(p -> p.getPath().toString()).getOrElse("(Create New) " + movie.getMaker().getStudio()));

        gridView.setCellFactory((GridView<PathCell> l) -> new DirectoryGridCell((s) -> {

        }));
*/
        //Vector<String> mappingData = Vector.of(RenameSettings.getInstance().getCompany());
        //.map(t->new MappingData(t.substring(0,t.indexOf("|") ),t.substring(t.indexOf("|")+1 )));

        JFXButton btnMapEditor = GUICommon.getBorderButton("  Mapping editor  ", (e) -> FXPathMappingEditor.show(runSelectDir));

        VBox vBox = GUICommon.getVBox(10, treeView, btnMapEditor, txtO, txtT);

        GUICommon.showDialog("Movie Directory :", vBox, "Cancel", "Okay", () -> {
            //if (returnPaths.getFinalPaths() != null) {
            call.accept(directoryEntry, getTargetPath(directoryEntry.getFilePath(), dest, txtT.getText()));
            //}
        });
    }

    private static String updateMappingDisplay(ObservableList<PathCell> observableList, final String maker) {
        String makerString = RenameSettings.getInstance().renameCompany(maker)
                .replace("/", "／");
        String text = "No Mapping Exist (Click M to Add)";
        for (PathCell pc : observableList) {
            pc.getIsUse().setValue(pc.getPath().equals(makerString));
            if (pc.getIsUse().get()) {
                text = "Mapping Exist : " + maker + " >> " + pc.getPath();
            }
        }

        return text;
    }

    @NotNull
    private static Tuple4<Path, Path, Boolean, Boolean> getTargetPath(@NotNull Path oldVideoPath, @NotNull Path destBase, String targetName) {
        Path pathOfMakerDir = destBase.resolve("(" + targetName + ")");
        Path pathOfVideoDir = pathOfMakerDir.resolve(oldVideoPath.getFileName());

        int x = 0;
        boolean alreadyExist = false;
        while (Files.exists(pathOfVideoDir)) {
            pathOfVideoDir = pathOfVideoDir.getParent().resolve(oldVideoPath.getFileName() + " (" + x + ")");
            alreadyExist = true;
            x++;
        }

        return Tuple.of(pathOfMakerDir, pathOfVideoDir, Files.exists(pathOfMakerDir), alreadyExist);
    }

    private static class ReturnPaths {
        Tuple4<Path, Path, Boolean, Boolean> finalPaths = null;
        final DirectoryEntry directoryEntry;

        @java.beans.ConstructorProperties({"directoryEntry"})
        public ReturnPaths(DirectoryEntry directoryEntry) {
            this.directoryEntry = directoryEntry;
        }

        public Tuple4<Path, Path, Boolean, Boolean> getFinalPaths() {
            return this.finalPaths;
        }

        public DirectoryEntry getDirectoryEntry() {
            return this.directoryEntry;
        }

        public void setFinalPaths(Tuple4<Path, Path, Boolean, Boolean> finalPaths) {
            this.finalPaths = finalPaths;
        }
    }

}
