package org.nagoya.controller;

import javafx.scene.Node;
import org.nagoya.GUICommon;
import org.nagoya.system.FXContextImp;
import org.nagoya.view.FXMangaPanelView;

public class FXMangaPanelControl extends FXContextImp {

    private static FXMangaPanelControl instance = null;

    private FXMangaPanelView panel;

    public static FXMangaPanelControl getInstance() {
        if (null == instance) {
            instance = new FXMangaPanelControl();
        }
        return instance;
    }

    private FXMangaPanelControl() {
        this.panel = GUICommon.loadFXMLController(FXMangaPanelView.class);
        if (this.panel == null) {
            throw new RuntimeException();
        }
        this.panel.setController(this);
    }

    @Override
    public Node getPane() {
        return this.panel.getPane();
    }

    public void executeSearch(String strUrl)
    {
        MangaParser.showWebView(strUrl);
    }
}
