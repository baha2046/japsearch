package org.nagoya.controller.siteparsingprofile.specific;

public class ArzonCSSQuery {
    public static String Q_ID         = "tr td:contains(品番：) + td";
    public static String Q_COVER      = "a[data-lightbox=jacket1]";
    public static String Q_PLOT       = "div.item_text";
    public static String Q_GENRES     = "tr td:contains(ジャンル：) + td";
    public static String Q_THUMBS     = "div[class=detail_img] a[data-lightbox=items]";
    public static String Q_ACTORS     = "tr td:contains(AV女優：) + td";
    public static String Q_TITLE      = "h1";
    public static String Q_SET        = "tr td:contains(シリーズ：) + td";
    public static String Q_RDATE      = "tr td:contains(発売日：) + td";
    public static String Q_VOTE       = "";
    public static String Q_TIME       = "tr td:contains(収録時間：) + td";
    public static String Q_DIRECTOR   = "tr td:contains(監督：) + td";
    public static String Q_STUDIO     = "tr td:contains(AVレーベル：) + td";
    public static String Q_MAKER      = "tr td:contains(AVメーカー：) + td";
}
