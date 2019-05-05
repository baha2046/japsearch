package org.nagoya.preferences;

import com.google.gson.Gson;
import io.vavr.collection.Stream;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.UtilCommon;
import org.nagoya.model.MovieV2;
import org.nagoya.system.Systems;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RenameSettings extends Settings {
    private static final String fileName = "naming.ini";
    private static final Gson gson = new Gson();

    private static RenameSettings INSTANCE = null;

    private String[] company;
    private String[] renameDirectoryFormat;
    private String[] renameFileFormat;

    private RenameSettings() {
        // use default setting
        this.company = new String[1];
        this.company[0] = "TEST1|TEST2";
        this.renameDirectoryFormat = new String[]{"#date", "space", "[", "#id", "]", "space", "#moviename"};
        this.renameFileFormat = new String[]{"[", "#id", "]"};
    }

    public static synchronized RenameSettings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = readSetting();
        }
        return INSTANCE;
    }

    private static RenameSettings readSetting() {
        Path path = Paths.get(fileName);

        if (Files.exists(path)) {
            GUICommon.debugMessage(() -> ">> Read Setting File : " + fileName);
            String str = UtilCommon.readStringFromFile(path);

            try {
                return gson.fromJson(str, RenameSettings.class);
            } catch (RuntimeException e) {
                GUICommon.debugMessage(() -> ">> Error Read Setting >> Wrong Jon Format");
            }
        }

        return new RenameSettings();
    }

    public void writeSetting() {
        GUICommon.debugMessage(() -> ">> Save Setting File : " + fileName);
        UtilCommon.saveStringToFile(Paths.get(fileName), gson.toJson(this));
    }

    @NotNull
    public static String getSuitableDirectoryName(MovieV2 movieV2) {
        StringBuilder stringBuilder = new StringBuilder();

        Stream.of(getInstance().getRenameDirectoryFormat())
                .map((s) -> MovieV2.getFormatUnit(movieV2, s))
                .forEach(stringBuilder::append);

        return stringBuilder.toString().replace(":", " ");
    }

    @NotNull
    public static String getSuitableFileName(MovieV2 movieV2) {
        StringBuilder stringBuilder = new StringBuilder();

        Stream.of(getInstance().getRenameFileFormat())
                .map((s) -> MovieV2.getFormatUnit(movieV2, s))
                .forEach(stringBuilder::append);

        return stringBuilder.toString();
    }

    @NotNull
    @Contract(pure = true)
    public static String getFileNameNfo(String inMovieName) {
        if (Systems.getPreferences().getNfoNamedMovieDotNfo()) {
            return "movie.nfo";
        } else {
            return (inMovieName + ".nfo");
        }
    }

    @NotNull
    @Contract(pure = true)
    public static String getFileNameFrontCover(String inMovieName) {
        if (Systems.getPreferences().getNoMovieNameInImageFiles()) {
            return "poster.jpg";
        } else {
            return (inMovieName + "-poster.jpg");
        }
    }

    @NotNull
    @Contract(pure = true)
    public static String getFileNameBackCover(String inMovieName) {
        if (Systems.getPreferences().getNoMovieNameInImageFiles()) {
            return "fanart.jpg";
        } else {
            return (inMovieName + "-fanart.jpg");
        }
    }

    @NotNull
    @Contract(pure = true)
    public static String getFolderNameActors() {
        return ".actors";
    }

    @NotNull
    @Contract(pure = true)
    public static String getFileNameFolderJpg() {
        return "folder.jpg";
    }

    @NotNull
    @Contract(pure = true)
    public static String getFileNameExtraImage() {
        return "thumb";
    }

    @NotNull
    @Contract(pure = true)
    public static String getFolderNameExtraImage() {
        return "extrathumbs";
    }

    @NotNull
    @Contract(pure = true)
    public static String getFileNameTrailer(String inMovieName) {
        return (inMovieName + "-trailer.mp4");
    }


    public String renameCompany(String inString) {
        return Stream.of(this.company).filter(s -> s.substring(0, s.indexOf("|")).equals(inString))
                .map(s -> s.substring(s.indexOf("|") + 1)).getOrElse(inString);
    }

    public void updateRenameMapping(String inString) {
        String[] strings = this.getCompany();
        boolean isUpdated = false;
        for (int x = 0; x < strings.length; x++) {
            if (inString.substring(0, inString.indexOf("|")).equals(strings[x].substring(0, strings[x].indexOf("|")))) {
                strings[x] = inString;
                isUpdated = true;
                break;
            }
        }
        if (!isUpdated) {
            String[] newStrings = new String[strings.length + 1];
            System.arraycopy(strings, 0, newStrings, 0, strings.length);
            newStrings[newStrings.length - 1] = inString;
            this.setCompany(newStrings);
        }
    }

    public String[] getCompany() {
        return this.company;
    }

    public String[] getRenameDirectoryFormat() {
        return this.renameDirectoryFormat;
    }

    public String[] getRenameFileFormat() {
        return this.renameFileFormat;
    }

    public void setCompany(String[] company) {
        this.company = company;
    }

    public void setRenameDirectoryFormat(String[] renameDirectoryFormat) {
        this.renameDirectoryFormat = renameDirectoryFormat;
    }

    public void setRenameFileFormat(String[] renameFileFormat) {
        this.renameFileFormat = renameFileFormat;
    }


}
