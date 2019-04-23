package org.nagoya.model.xmlserialization;


import org.nagoya.model.dataitem.FxThumb;

/**
 * Helper class for serializing a fanart object to and from XML
 */
public class KodiXmlFanartBean {
    private String[] thumb;

    public KodiXmlFanartBean(String[] thumb) {
        super();
        this.thumb = thumb;
    }

    public KodiXmlFanartBean(FxThumb[] thumb) {
        if (thumb.length == 0) {
            this.thumb = new String[0];
        } else {
            this.thumb = new String[thumb.length];
            for (int i = 0; i < thumb.length; i++) {
                this.thumb[i] = thumb[i].getThumbURL().toString();
            }
        }
    }

    public String[] getThumb() {
        return this.thumb;
    }

    public void setThumb(String[] thumb) {
        this.thumb = thumb;
    }

    public FxThumb[] toFanart()  {
        FxThumb[] fanart = new FxThumb[this.thumb.length];
        for (int i = 0; i < fanart.length; i++) {
            fanart[i] = FxThumb.of(this.thumb[i]).get();
        }
        return fanart;
    }
}
