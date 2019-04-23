package org.nagoya.view.editor;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.nagoya.GUICommon;
import org.nagoya.preferences.GeneralSettings;
import org.nagoya.preferences.Settings;
import org.nagoya.system.Systems;

public class FXSettingEditor {

    public static void showSettingEditor() {

        VBox dialogVbox1 = new VBox();
        dialogVbox1.setAlignment(Pos.CENTER_LEFT);
        dialogVbox1.setSpacing(10);
        //dialogVbox1.setMinSize(900, 800);

        GeneralSettings perference = Systems.getPreferences();
        //var Sel = new ArrayList<CheckBox>();
        CheckBox Sel1 = new CheckBox("Write Fanart And Posters");
        Sel1.setSelected(perference.getWriteFanartAndPostersPreference());

        CheckBox Sel2 = new CheckBox("Is First Word FileID");
        Sel2.setSelected(perference.getIsFirstWordOfFileID());

        CheckBox Sel3 = new CheckBox("Rename Movie File");
        Sel3.setSelected(perference.getRenameMovieFile());

        CheckBox Sel4 = new CheckBox("Extra Fanart Scraping Enabled");
        Sel4.setSelected(perference.getExtraFanartScrapingEnabledPreference());

        CheckBox Sel5 = new CheckBox("Scrape Actor");
        Sel5.setSelected(perference.getScrapeActor());

        CheckBox Sel6 = new CheckBox("Overwrite Fanart And Posters");
        Sel6.setSelected(perference.getOverWriteFanartAndPostersPreference());

        CheckBox Sel7 = new CheckBox("Download Actor Images To Actor Folder");
        Sel7.setSelected(perference.getDownloadActorImagesToActorFolderPreference());

        CheckBox Sel8 = new CheckBox("No Movie Name In Image Files");
        Sel8.setSelected(perference.getNoMovieNameInImageFiles());

        CheckBox Sel9 = new CheckBox("Nfo Named movie.nfo");
        Sel9.setSelected(perference.getNfoNamedMovieDotNfo());

        CheckBox Sel10 = new CheckBox("Confirm Clean Up File Name Before Renaming");
        Sel10.setSelected(perference.getConfirmCleanUpFileNameNameBeforeRenaming());

        CheckBox Sel11 = new CheckBox("Append ID To Start Of Title");
        Sel11.setSelected(perference.getAppendIDToStartOfTitle());

        CheckBox Sel12 = new CheckBox("Use Filename As Title");
        Sel12.setSelected(perference.getUseFileNameAsTitle());

        dialogVbox1.getChildren().addAll(Sel2, Sel11, Sel12, Sel3, Sel4, Sel1, Sel7, Sel6, Sel5, Sel8, Sel9, Sel10);

        GUICommon.showDialog("Setting :", dialogVbox1, "Cancel", "Apply Change", () -> {
            perference.setWriteFanartAndPostersPreference(Sel1.isSelected());
            perference.setIsFirstWordOfFileID(Sel2.isSelected());
            perference.setRenameMovieFile(Sel3.isSelected());
            perference.setExtraFanartScrapingEnabledPreference(Sel4.isSelected());
            perference.setScrapeActor(Sel5.isSelected());
            perference.setOverWriteFanartAndPostersPreference(Sel6.isSelected());
            perference.setDownloadActorImagesToActorFolderPreference(Sel7.isSelected());
            perference.setNoMovieNameInImageFiles(Sel8.isSelected());
            perference.setNfoNamedMovieDotNfo(Sel9.isSelected());
            perference.setConfirmCleanUpFileNameNameBeforeRenaming(Sel10.isSelected());
            perference.setAppendIDToStartOfTitle(Sel11.isSelected());
            perference.setUseFileNameAsTitle(Sel12.isSelected());
            Settings.savePreferences();
        });
    }
}
