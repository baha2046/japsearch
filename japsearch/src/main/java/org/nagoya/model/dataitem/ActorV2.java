package org.nagoya.model.dataitem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vavr.control.Option;
import io.vavr.gson.VavrGson;
import org.nagoya.GUICommon;
import org.nagoya.UtilCommon;
import org.nagoya.system.Systems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActorV2 {

    public enum Source {
        DUGA, DMM, JAVBUS, LOCAL, NONE
    }

    public static ActorV2 of(String strName)
    {
        return ActorV2.of(strName, Source.NONE, "", "", "");
    }

    public static ActorV2 of(String strName, Source source, String strUrl, String imgUrl, String strDesc)
    {
        ActorV2 actorV2 = Systems.getActorDB().getActors().get(strName);

        if(actorV2 == null)
        {
            actorV2 = new ActorV2(strName, source, strUrl, imgUrl, strDesc);
            Systems.getActorDB().getActors().put(strName, actorV2);
        }
        else
        {
            actorV2.addRecord(source, strUrl, imgUrl, strDesc);
        }

        GsonBuilder builder = new GsonBuilder();
        VavrGson.registerAll(builder);
        Gson gSon = builder.create();
        GUICommon.debugMessage(gSon.toJson(actorV2));

        //Systems.getActorDB().getActors().forEach((n,a)->GUICommon.debugMessage(n));

        return actorV2;
    }

    private String name;

    private List<Option<NetRecord>> data;

    private Option<FxThumb> localImage = Option.none();

    private Actor oldActor = null;

    private ActorV2(String strName, Source siteID, String url, String imgUrl, String desc)
    {
        data = new ArrayList<>();
        clearSource();

        oldActor = new Actor(strName, "", getImage().getOrNull());
        setName(strName);

        addRecord(siteID, url, imgUrl, desc);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void clearSource()
    {
        for(int x = 0; x < Source.values().length; x++)
        {
            data.add(Option.none());
        }
    }

    public void setLocalImage(FxThumb imgLocal)
    {
        data.set(Source.LOCAL.ordinal(), Option.of(new NetRecord(Source.LOCAL, Option.of(imgLocal), "", "")));
    }

    public void addRecord(Source siteID, String url, String imgUrl, String desc)
    {
        if(siteID == Source.NONE) return;

        GUICommon.debugMessage("Add Record = " + imgUrl);

        Option<FxThumb> fxThumb = Option.none();
        if(!Objects.equals(imgUrl, "")) fxThumb = FxThumb.of(imgUrl);

        data.set(siteID.ordinal(), Option.of(new NetRecord(siteID, fxThumb, url, desc)));
    }

    public Option<FxThumb> getImage()
    {
        return getImage(localImage);
    }

    public Option<FxThumb> getNetImage()
    {
        return getImage(Option.none());
    }

    private Option<FxThumb> getImage(Option<FxThumb> fxThumbs)
    {
        for(Option<NetRecord> record : data)
        {
            if(fxThumbs.isEmpty()) fxThumbs = record.map(NetRecord::getActorImage).getOrElse(Option.none());
        }

        return fxThumbs;
    }

    @Deprecated
    public Actor getActor()
    {
        return oldActor;
    }

    public void writeImageToFile(File fileNameToWrite) throws IOException
    {
        getImage().peek(t-> UtilCommon.saveFile(t.getThumbURL(), fileNameToWrite, null));
    }

    class NetRecord {
        private final Source siteID;
        private final Option<FxThumb> actorImage;
        private final String url;
        private final String desc;

        @java.beans.ConstructorProperties({"siteID", "actorImage", "url", "desc"})
        public NetRecord(Source siteID, Option<FxThumb> actorImage, String url, String desc) {
            this.siteID = siteID;
            this.actorImage = actorImage;
            this.url = url;
            this.desc = desc;
        }

        public Source getSiteID() {
            return this.siteID;
        }

        public Option<FxThumb> getActorImage() {
            return this.actorImage;
        }

        public String getUrl() {
            return this.url;
        }

        public String getDesc() {
            return this.desc;
        }

        public String toString() {
            return "ActorV2.NetRecord(siteID=" + this.siteID + ", actorImage=" + this.actorImage + ", url=" + this.url + ", desc=" + this.desc + ")";
        }
    }

}
