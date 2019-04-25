package org.nagoya.controller.siteparsingprofile.specific;

public class DugaCSSQuery {
    public static String Q_ID         = "tr th:contains(メーカー品番) + td";
    public static String Q_COVER      = "a[itemprop=image]";
    public static String Q_PLOT       = "p[class=introduction]";
    public static String Q_GENRES     = "tr th:contains(カテゴリ) + td";
    public static String Q_THUMBS     = "ul[id=digestthumbbox]";
    public static String Q_ACTORS     = "ul[class=performer]";
    public static String Q_TITLE      = "h1[class=title]";
    public static String Q_SET        = "tr th:contains(シリーズ) + td";
    public static String Q_RDATE      = "tr th:contains(発売日) + td span";
    public static String Q_VOTE       = "";
    public static String Q_TIME       = "tr th:contains(再生時間) + td";
    public static String Q_DIRECTOR   = "tr th:contains(監督) + td";
    public static String Q_STUDIO     = "tr th:contains(レーベル) + td";
    public static String Q_MAKER      = "tr th:contains(メーカー) + td";
}

