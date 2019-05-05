package org.nagoya.model.xmlserialization;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import io.vavr.control.Option;
import org.nagoya.model.dataitem.FxThumb;

@XStreamAlias("thumb")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"img"})
public class KodiXmlThumbBean {

    String aspect;
    String img;

    public KodiXmlThumbBean(String aspect, String img) {
        this.aspect = aspect;
        this.img = img;
    }

    public Option<FxThumb> toThumb() {
        return FxThumb.of(this.img);
    }
}
