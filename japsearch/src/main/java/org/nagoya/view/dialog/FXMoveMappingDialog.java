package org.nagoya.view.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import cyclops.control.Future;
import io.vavr.Tuple;
import io.vavr.Tuple4;
import io.vavr.collection.Vector;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.GridView;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.model.MovieV2;
import org.nagoya.model.PathCell;
import org.nagoya.preferences.RenameSettings;
import org.nagoya.system.DirectorySystem;
import org.nagoya.system.Systems;
import org.nagoya.view.customcell.DirectoryGridCell;
import org.nagoya.view.editor.FXPathMappingEditor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

public class FXMoveMappingDialog {

    public static void show(ObservableList<Path> paths, @NotNull DirectoryEntry directoryEntry,
                            BiConsumer<DirectoryEntry, Tuple4> call) {

        GridView<PathCell> gridView = new GridView<>();

        MovieV2 movie = directoryEntry.getMovieData();
        Path dest = Paths.get("x:/Movies/av/");

        Future<Vector<PathCell>> vectorFuture = Future.of(()-> DirectorySystem.readPath(dest)
                .filter(Files::isDirectory)
                .map(p -> p.getFileName().toString())
                .map(s -> s.startsWith("(") ? s.substring(1, s.length() - 1) : s)
                .map(PathCell::new), Systems.getExecutorServices());

        ObservableList<PathCell> stringObservableList = FXCollections.observableArrayList(PathCell.extractor());

        gridView.setMinWidth(1200);
        gridView.setMaxHeight(500);
        gridView.setMinHeight(500);
        gridView.setCellWidth(180);
        gridView.setCellHeight(40);
        gridView.setHorizontalCellSpacing(4);
        gridView.setVerticalCellSpacing(4);
        gridView.setItems(stringObservableList);
        gridView.autosize();

        JFXTextField out2 = GUICommon.getTextField("", 400);
        JFXTextField out3 = GUICommon.getTextField("", 700);

        ReturnPaths returnPaths = new ReturnPaths(directoryEntry);

        vectorFuture.peek(pathCells -> {
            String makerStringAfterMapping = RenameSettings.getInstance().renameCompany(movie.getMovieMaker().getStudio())
                    .replace("/", "／");
            Platform.runLater(()->{
                stringObservableList.addAll(pathCells.toJavaList());
                out2.setText(updateMappingDisplay(stringObservableList, makerStringAfterMapping));
                returnPaths.setFinalPaths(getTargetPath(directoryEntry.getFilePath(), dest, makerStringAfterMapping));
                out3.setText("Target : " + returnPaths.getFinalPaths()._2().toString());
            });
        });

        //JFXTextField out1 = new JFXTextField();
        //out1.setText("Target: " + findPath.map(p -> p.getPath().toString()).getOrElse("(Create New) " + movie.getMaker().getStudio()));

        gridView.setCellFactory((GridView<PathCell> l) -> new DirectoryGridCell((s)->{
            final String newMapping = movie.getMovieMaker().getStudio() + " >> " + s;
            GUICommon.showDialog("Confirm :", new Text("Set [ " + newMapping + " ] as new mapping ?"), "Cancel",
                    "Okay", ()->{
                        RenameSettings.getInstance().updateRenameMapping(newMapping.replace(" >> ","|"));
                        RenameSettings.getInstance().writeSetting();
                        out2.setText(updateMappingDisplay(stringObservableList, movie.getMovieMaker().getStudio()));
                        returnPaths.setFinalPaths(getTargetPath(directoryEntry.getFilePath(), dest, s));
                        out3.setText("Target : " + returnPaths.getFinalPaths()._2().toString());
                    });
        }));

        //Vector<String> mappingData = Vector.of(RenameSettings.getInstance().getCompany());
                //.map(t->new MappingData(t.substring(0,t.indexOf("|") ),t.substring(t.indexOf("|")+1 )));

        JFXButton btnMapEditor = GUICommon.getButton("[ Mapping editor ]", (e)-> FXPathMappingEditor.show(()->{
            out2.setText(updateMappingDisplay(stringObservableList, movie.getMovieMaker().getStudio()));
        }));

        HBox hBox = GUICommon.getHBox(80, out2, out3);
        hBox.setAlignment(Pos.BASELINE_LEFT);
        VBox vBox = GUICommon.getVBox(10, gridView, btnMapEditor, hBox);

        GUICommon.showDialog("Movie Directory :", vBox, "Cancel", "Okay", () -> {
            if(returnPaths.getFinalPaths() != null)
            {
                call.accept(returnPaths.getDirectoryEntry(), returnPaths.getFinalPaths());
            }
        });
    }

    private static String updateMappingDisplay(ObservableList<PathCell> observableList, final String maker)
    {
        String makerString = RenameSettings.getInstance().renameCompany(maker)
                .replace("/", "／");
        String text = "No Mapping Exist (Click M to Add)";
        for(PathCell pc: observableList)
        {
            pc.getIsUse().setValue(pc.getPath().equals(makerString));
            if(pc.getIsUse().get())
            {
                text = "Mapping Exist : " + maker + " >> " +pc.getPath();
            }
        }

        return text;
    }

    @NotNull
    private static Tuple4<Path, Path, Boolean, Boolean> getTargetPath(@NotNull Path oldVideoPath, @NotNull Path destBase, String targetName)
    {
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

    private static class ReturnPaths
    {
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
