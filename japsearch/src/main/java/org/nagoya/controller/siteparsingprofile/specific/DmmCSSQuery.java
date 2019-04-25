package org.nagoya.controller.siteparsingprofile.specific;

public class DmmCSSQuery {
    public static String Q_ID         = "td:containsOwn(品番：) ~ td";
    public static String Q_COVER      = "a[name=package-image], div#sample-video img[src*=/pics.dmm.co.jp]";
    public static String Q_PLOT       = "";
    public static String Q_GENRES     = "table.mg-b12 tr td a[href*=article=keyword/id=]";
    public static String Q_THUMBS     = "div#sample-image-block img.mg-b6";
    public static String Q_ACTORS     = "";
    public static String Q_TITLE      = "[property=og:title]";
    public static String Q_SET        = "table.mg-b20 tr td a[href*=article=series/id=]";
    public static String Q_RDATE      = "table.mg-b20 tr td:contains(貸出開始日：) + td, table.mg-b20 tr td:contains(発売日：) + td, table.mg-b20 tr td:contains(商品発売日：) + td";
    public static String Q_VOTE       = ".d-review__evaluates strong";
    public static String Q_TIME       = "table.mg-b20 tr td:contains(収録時間：) + td";
    public static String Q_DIRECTOR   = "table.mg-b20 tr td a[href*=article=director/id=]";
    public static String Q_STUDIO     = "table.mg-b20 tr td a[href*=article=label/id=]";
    public static String Q_MAKER      = "table.mg-b20 tr td a[href*=article=maker/id=]";
}
