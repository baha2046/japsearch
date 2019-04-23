package org.nagoya;

import io.vavr.control.Try;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;

public class UtilCommon {

    public static boolean renameFile(@NotNull Path oldName, @NotNull Path newName) {
        return Try.of(()->Files.move(oldName, newName))
                .fold(e->{
                        GUICommon.debugMessage(() -> "renameFile >> ------------ RENAME FAILED ------------");
                        return false;
                    }, p-> true);
    }

    public static void delFile(Path file, Runnable onSuccess) {

        Try.run(()->Files.delete(file)).onFailure(e->{
            if(e instanceof DirectoryNotEmptyException)
            {
                GUICommon.showDialog("Directory is not Empty :", null, "Cancel", "Cont", ()->{
                     Try.run(()-> {
                                Files.walk(file)
                                        .sorted(Comparator.reverseOrder())
                                        .map(Path::toFile)
                                        .forEach(File::delete);
                     }).onSuccess(v->onSuccess.run());
                });
            }
            else
            {
                GUICommon.debugMessage(() -> "delFile >> ------------ DELETE FAILED ------------");
            }

        }).onSuccess(v->onSuccess.run());
    }


}
