package org.nagoya.controller.siteparsingprofile.specific;

public class JavBusCSSQuery {
    public static String Q_ID         = "p:contains(品番:) > span + span";
    public static String Q_COVER      = "a.bigImage";
    public static String Q_PLOT       = "";
    public static String Q_GENRES     = "span.genre a[href*=/genre/]";
    public static String Q_THUMBS     = "a.sample-box";
    public static String Q_ACTORS     = "div.star-box li a img";
    public static String Q_TITLE      = "div.container h3";
    public static String Q_SET        = "";
    public static String Q_RDATE      = "";
    public static String Q_VOTE       = "";
    public static String Q_TIME       = "";
    public static String Q_DIRECTOR   = "";
    public static String Q_STUDIO     = "";
    public static String Q_MAKER      = "";
}

