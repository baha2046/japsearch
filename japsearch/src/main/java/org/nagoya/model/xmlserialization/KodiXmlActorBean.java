package org.nagoya.model.xmlserialization;

import org.nagoya.model.dataitem.ActorV2;

/**
 * Helper class for serializing a actor object to and from XML
 */
public class KodiXmlActorBean {

    public String name;
    public String role;
    public String thumb;

    public KodiXmlActorBean(String name, String role, String thumb) {
        super();
        this.name = name;
        this.role = role;
        this.thumb = thumb;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getThumb() {
        return this.thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public ActorV2 toActor() {
        return ActorV2.of(this.name, ActorV2.Source.LOCAL, "", this.thumb, "");
    }

}
