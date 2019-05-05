package org.nagoya.model.xmlserialization;

import org.nagoya.model.dataitem.Set;

public class KodiXmlSetBean {

    public String name;
    public String overview;

    public KodiXmlSetBean(String name, String overview) {
        this.name = name;
        this.overview = overview;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOverview() {
        return this.overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Set toSet() {
        return new Set(this.name);
    }

}