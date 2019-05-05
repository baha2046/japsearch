package org.nagoya.model.xmlserialization;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import org.nagoya.model.dataitem.FxThumb;

import java.util.Collection;

public class XStreamForNfo {

    private static XStreamForNfo instance = null;

    private final XStream xstream;

    static XStream getXStream() {
        if (null == instance) {
            instance = new XStreamForNfo();
        }
        return instance.get();
    }

    private XStreamForNfo() {
        this.xstream = new XStream(new DomDriver("UTF-8")) {
            @Override
            protected void setupConverters() {
            }
        };
        this.xstream.registerConverter(new ReflectionConverter(this.xstream.getMapper(), this.xstream.getReflectionProvider()), XStream.PRIORITY_VERY_LOW);
        this.xstream.registerConverter(new IntConverter(), XStream.PRIORITY_NORMAL);
        this.xstream.registerConverter(new StringConverter(), XStream.PRIORITY_NORMAL);
        this.xstream.registerConverter(new CollectionConverter(this.xstream.getMapper()), XStream.PRIORITY_NORMAL);


        this.xstream.addPermission(NullPermission.NULL);
        this.xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        this.xstream.allowTypeHierarchy(Collection.class);

        this.xstream.omitField(FxThumb.class, "thumbImage");
        this.xstream.processAnnotations(KodiXmlUniqueidBean.class);
        this.xstream.processAnnotations(KodiXmlThumbBean.class);
        this.xstream.alias("movie", KodiXmlMovieBean.class);
        this.xstream.alias("actor", KodiXmlActorBean.class);
        this.xstream.alias("fanart", KodiXmlFanartBean.class);
        this.xstream.alias("set", KodiXmlSetBean.class);
        this.xstream.addImplicitCollection(KodiXmlMovieBean.class, "actor");
        this.xstream.addImplicitArray(KodiXmlMovieBean.class, "director", "director");
        this.xstream.addImplicitArray(KodiXmlFanartBean.class, "thumb", "thumb");
        this.xstream.addImplicitArray(KodiXmlMovieBean.class, "genre", "genre");
        this.xstream.addImplicitArray(KodiXmlMovieBean.class, "thumb", "thumb");

        this.xstream.ignoreUnknownElements();
    }

    public XStream get() {
        return this.xstream;
    }
}
