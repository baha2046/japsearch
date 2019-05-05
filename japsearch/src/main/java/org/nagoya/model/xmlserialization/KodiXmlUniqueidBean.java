package org.nagoya.model.xmlserialization;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("uniqueid")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"id"})
public class KodiXmlUniqueidBean {

    public String type = "home";

    @XStreamAlias("default")
    public String pdefault = "true";

    public String id;

    public KodiXmlUniqueidBean(String id) {
        this.id = id;
    }

}