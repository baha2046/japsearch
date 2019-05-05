package org.nagoya.controller.siteparsingprofile.specific;

import io.vavr.collection.Stream;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nagoya.GUICommon;
import org.nagoya.controller.languagetranslation.Language;
import org.nagoya.controller.siteparsingprofile.SiteParsingProfile;
import org.nagoya.model.SearchResult;
import org.nagoya.model.dataitem.Runtime;
import org.nagoya.model.dataitem.*;
import org.nagoya.preferences.GeneralSettings;
import org.nagoya.system.ExecuteSystem;
import org.nagoya.system.Systems;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DugaParsingProfile extends SiteParsingProfile implements SpecificProfile {
    private URL refererURL;

    public DugaParsingProfile() {
        super();
        this.setScrapingLanguage(Language.JAPANESE);
    }

    public DugaParsingProfile(Document document) {
        super(document);
        this.setScrapingLanguage(Language.JAPANESE);
    }

    /**
     * Default constructor does not define a document, so be careful not to call
     * scrape methods without initializing the document first some other way.
     * This constructor is mostly used for calling createSearchString() and
     * getSearchResults()
     */
    public DugaParsingProfile(boolean doGoogleTranslation) {
        super();
        this.setScrapingLanguage(Language.JAPANESE);
    }

    public DugaParsingProfile(boolean doGoogleTranslation, boolean scrapeTrailers) {
        super();
        this.setScrapingLanguage(Language.JAPANESE);
    }

    public DugaParsingProfile(Document document, boolean doGoogleTranslation) {
        super(document);
        this.setScrapingLanguage(Language.JAPANESE);
    }

    public static String fixUpIDFormatting(String idElementText) {
        // remove useless word
        if (idElementText.contains("廃盤")) {
            idElementText = idElementText.substring(0, idElementText.indexOf('廃') - 2);
        }
        return idElementText;
    }

    public static String parserName() {
        return "duga.jp";
    }

    @Override
    public List<ScraperGroupName> getScraperGroupNames() {
        if (this.groupNames == null) {
            this.groupNames = Collections.singletonList(ScraperGroupName.JAV_CENSORED_SCRAPER_GROUP);
        }
        return this.groupNames;
    }

    @Override
    public Title scrapeTitle() {
        Element titleElement = this.document.select(DugaCSSQuery.Q_TITLE).first();
        if (titleElement != null) {
            return new Title(titleElement.text());
        } else {
            return new Title("");
        }
    }

    @Override
    public Option<Set> scrapeSet() {
        return this.defaultScrapeSet(DugaCSSQuery.Q_SET);
    }

    @Override
    public Option<ReleaseDate> scrapeReleaseDate() {
        Element releaseDateElement = this.document.select(DugaCSSQuery.Q_RDATE).first();

        if (releaseDateElement != null) {
            String releaseDate = releaseDateElement.attr("content");
            releaseDate = StringUtils.replace(releaseDate, "/", "-");
            return Option.of(new ReleaseDate(releaseDate));
        }
        return Option.none();
    }

    @Override
    public Option<Plot> scrapePlot() {
        Element plotElement = this.document.select(DugaCSSQuery.Q_PLOT).first();
        return (plotElement == null) ? Option.none() : Option.of(new Plot((plotElement.text())));
    }

    @Override
    public Option<Runtime> scrapeRuntime() {
        Element runtimeElement = this.document.select(DugaCSSQuery.Q_TIME).first();

        if (runtimeElement != null && runtimeElement.text().length() > 0) {
            // of rid of japanese word for minutes and just of the number
            System.out.println("scrapeRuntime() >> " + runtimeElement.text());
            return Option.of(new Runtime(runtimeElement.text().substring(0, runtimeElement.text().indexOf("分"))));//.replaceAll("分", "");
        }

        return Option.none();
    }

    @Override
    public Trailer scrapeTrailer() {

        return Trailer.BLANK_TRAILER;
    }

    @Override
    public Option<FxThumb> scrapeCover() {
        Element postersElement = this.document.select(DugaCSSQuery.Q_COVER).first();

        if (postersElement != null) {
            String posterLink = postersElement.attr("href");
            return FxThumb.of(posterLink);
        }
        return Option.none();
    }

    @Override
    public Stream<FxThumb> scrapeExtraImage() {
        Element extraArtE = this.document.select(DugaCSSQuery.Q_THUMBS).first();
        Elements extraArtElements = extraArtE.select("a");

        if (null == extraArtElements || extraArtElements.size() > 20) {
            return Stream.empty();
        }

        return Stream.ofAll(extraArtElements.stream())
                .map((ex) -> ex.attr("href"))
                .filter(Objects::nonNull)
                .peek((link) -> System.out.println("scrapeExtraArt() >> " + link))
                .map(FxThumb::of)
                .flatMap(Option::toStream);
    }

    @Override
    public ID scrapeID() {
        Element idElement = this.document.select(DugaCSSQuery.Q_ID).first();
        if (idElement != null) {
            String idElementText = idElement.text();
            idElementText = fixUpIDFormatting(idElementText);

            System.out.println("scrapeID() >> " + idElementText);

            return new ID(idElementText);
        }
        //This page didn't have an ID, so just put in a empty one
        else {
            return ID.BLANK_ID;
        }
    }

    @Override
    public ArrayList<Genre> scrapeGenres() {
        int genre_num = 0;

        Element genre = this.document.select(DugaCSSQuery.Q_GENRES).first();
        Elements genreElements = genre.select("a");

        // Arzon genres divided into parent and child cat.,
        // if there has a child cat. add the child otherwise add the parent cat.
        ArrayList<String> genresList = new ArrayList<>(genreElements.size());
        for (Element genreElement : genreElements) {
            // Element currentElement = genreElement.select("li[class=child]").first();

            if (genreElement != null) {
                String genreStr = genreElement.text();

                if (!Objects.equals(genreStr, "アダルト")) {
                    genresList.add(genreStr);
                    genre_num++;
                }
            }
        }

        ArrayList<Genre> genresReturn = new ArrayList<>(genre_num);
        for (int x = 0; x < genre_num; x++) {
            genresReturn.add(new Genre(genresList.get(x)));
        }
        return genresReturn;
    }

    @Override
    public Future<List<ActorV2>> scrapeActorsAsync() {
        return Future.of(Systems.getExecutorServices(ExecuteSystem.role.NORMAL), () ->
        {
            List<ActorV2> observableList = new ArrayList<>();

            Element actressIDElements = this.document.select(DugaCSSQuery.Q_ACTORS).first();

            if (actressIDElements != null) {
                Elements actressURL = actressIDElements.select("a");

                for (Element actressIDLink : actressURL) {

                    String actressIDHref = actressIDLink.attr("abs:href");
                    String actressName = actressIDLink.text();

                    ActorV2 actor = DmmParsingProfile.scrapeActorThumbFromDmm(actressName);

                    GUICommon.debugMessage("scrapeActorsAsync() >> " + actressName + " " + actressIDHref);

                    observableList.add(actor);
                }
            }

            return observableList;
        });
    }


    @Override
    public ArrayList<Director> scrapeDirectors() {
        return this.defaultScrapeDirectors(DugaCSSQuery.Q_DIRECTOR);
    }

    @Override
    public Option<Studio> scrapeStudio() {
        return this.defaultScrapeStudio(DugaCSSQuery.Q_STUDIO);
    }

    @Override
    public Studio scrapeMaker() {
        return this.defaultScrapeMaker(DugaCSSQuery.Q_MAKER);
    }

    @Override
    public String createSearchString(File file, String searchStr) {
        this.scrapedMovieFile = file;

        URLCodec codec = new URLCodec();
        try {
            String fileNameURLEncoded = codec.encode(searchStr);
            //system.out.println("FileNameUrlencode = " + fileNameURLEncoded);
            this.refererURL = new URL("https://duga.jp/search/=/q=" + fileNameURLEncoded + "/");

            return this.refererURL.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * returns a String[] filled in with urls of each of the possible movies
     * found on the page returned from createSearchString
     *
     * @throws IOException
     */
    @Override
    public SearchResult[] getSearchResults(String searchString) throws IOException {

        System.out.println(">> getSearchResults " + searchString);

        ArrayList<SearchResult> linksList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(searchString).userAgent("Mozilla").ignoreHttpErrors(true).timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).get();

            Elements videoLinksElements = doc.select("div[class=contentslist]");
            if (videoLinksElements != null) {

                for (Element videoLink : videoLinksElements) {
                    String currentLink = videoLink.select("a").first().attr("abs:href");
                    String currentLinkLabel = videoLink.select("div[class=title]").first().text();
                    String currentLinkImage = videoLink.select("img").first().attr("src");

                    if (currentLink.length() > 1) {
                        linksList.add(new SearchResult(currentLink, currentLinkLabel, FxThumb.of(currentLinkImage)));
                    }
                }
            }
            return linksList.toArray(new SearchResult[linksList.size()]);
        } catch (IOException e) {
            e.printStackTrace();
            return new SearchResult[0];
        }
    }

    @Override
    public String toString() {
        return "duga.jp";
    }

    @Override
    public SiteParsingProfile newInstance() {
        GeneralSettings preferences = Systems.getPreferences();
        return new DugaParsingProfile(!preferences.getScrapeInJapanese());
    }

    @Override
    public String getParserName() {
        return parserName();
    }
}
