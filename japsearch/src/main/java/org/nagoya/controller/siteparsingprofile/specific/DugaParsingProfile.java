package org.nagoya.controller.siteparsingprofile.specific;

import io.vavr.collection.Stream;
import io.vavr.control.Option;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import org.nagoya.model.dataitem.*;
import org.nagoya.model.dataitem.Runtime;
import org.nagoya.preferences.GeneralSettings;
import org.nagoya.system.Systems;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
            this.groupNames = Arrays.asList(ScraperGroupName.JAV_CENSORED_SCRAPER_GROUP);
        }
        return this.groupNames;
    }

    @Override
    public Title scrapeTitle() {
        Element titleElement = this.document.select("h1[class=title]").first();
        // run a google translate on the japanese title

        if (titleElement != null) {
            System.out.println("scrapeTitle() >> " + titleElement.text());

            return new Title(titleElement.text());

        } else {
            return new Title("");
        }
    }

    @Override
    public OriginalTitle scrapeOriginalTitle() {
        Element titleElement = this.document.select("h1[class=title]").first();
        // leave the original title as the japanese title
        return new OriginalTitle(titleElement.text());
    }

    @Override
    public SortTitle scrapeSortTitle() {
        // we don't need any special sort title - that's usually something the
        // user provides
        return SortTitle.BLANK_SORTTITLE;
    }

    @Override
    public Option<Set> scrapeSet() {
        // I found that this シリーズ： is always empty so I cannot test it
        Element setElement = this.document.select("tr th:contains(シリーズ) + td").first();
        return (null == setElement) ? Option.none() : Option.of(new Set(setElement.text()));
    }

    @Override
    public Rating scrapeRating() {
        return Rating.BLANK_RATING;
    }

    @Override
    public Option<Year> scrapeYear() {
        return this.scrapeReleaseDate().map(ReleaseDate::getYear);
    }

    @Override
    public Option<ReleaseDate> scrapeReleaseDate() {
        Element releaseDateElement = this.document.select("tr th:contains(発売日) + td span").first();

        if (releaseDateElement != null) {
            String releaseDate = releaseDateElement.attr("content");
            //we want to convert something like 2015/04/25 to 2015-04-25
            releaseDate = StringUtils.replace(releaseDate, "/", "-");

            System.out.println("scrapeReleaseDate() >> " + releaseDate);

            return Option.of(new ReleaseDate(releaseDate));
        }
        return Option.none();
    }

    @Override
    public Top250 scrapeTop250() {
        return Top250.BLANK_TOP250;
    }

    @Override
    public Votes scrapeVotes() {
        return Votes.BLANK_VOTES;
    }

    @Override
    public Outline scrapeOutline() {
        // TODO Auto-generated method stub
        return Outline.BLANK_OUTLINE;
    }

    @Override
    public Option<Plot> scrapePlot() {
        Element plotElement = this.document.select("p[class=introduction]").first();

        return (plotElement == null) ? Option.none() : Option.of(new Plot((plotElement.text())));
    }

    @Override
    public Tagline scrapeTagline() {
        return Tagline.BLANK_TAGLINE;
    }

    @Override
    public Option<Runtime> scrapeRuntime() {
        Element runtimeElement = this.document.select("tr th:contains(再生時間) + td").first();

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
    @Deprecated
    public FxThumb[] scrapePosters() {
        return null;
    }

    @Override
    public Option<FxThumb> scrapeCover() {
        Element postersElement = this.document.select("a[itemprop=image]").first();

        if (postersElement != null) {
            String posterLink = postersElement.attr("href");
            System.out.println("scrapePostersAndFanart() >> " + posterLink);
            return FxThumb.of(posterLink);
        }
        return Option.none();
    }

    private FxThumb[] scrapeExtraArt() {
        Element extraArtE = this.document.select("ul[id=digestthumbbox]").first();
        Elements extraArtElements = extraArtE.select("a");

        ArrayList<FxThumb> thumbsList = new ArrayList<>(1 + extraArtElements.size());

        for (Element extraArt : extraArtElements) {
            String thumbsLink = extraArt.attr("href");

            if (thumbsLink != null) {
                System.out.println("scrapeExtraArt() >> " + thumbsLink);
                thumbsList.add(FxThumb.of(thumbsLink).get());
            }
        }

        return thumbsList.toArray(new FxThumb[0]);
    }

    @Override
    public Stream<FxThumb> scrapeExtraImage() {
        Element extraArtE = this.document.select("ul[id=digestthumbbox]").first();
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
    @Deprecated
    public FxThumb[] scrapeFanart() {
        return null;
    }

    @Override
    public Option<MPAARating> scrapeMPAA() {
        return Option.of(MPAARating.RATING_XXX);
    }

    @Override
    public ID scrapeID() {
        Element idElement = this.document.select("tr th:contains(メーカー品番) + td").first();
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

        Element genre = this.document.select("tr th:contains(カテゴリ) + td").first();
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
    public ArrayList<Actor> scrapeActors() {
        ArrayList<Actor> actorList = new ArrayList<>();

        Element actressIDElements = this.document.select("ul[class=performer]").first();

        if (actressIDElements != null) {
            Elements actressURL = actressIDElements.select("a");

            System.out.println("scrapeActors() >> " + actressURL.size());
            List<Runnable> tasks = new ArrayList<>();

            for (Element actressIDLink : actressURL) {
                tasks.add(() -> {
                    String actressIDHref = actressIDLink.attr("abs:href");
                    String actressName = actressIDLink.text();

                  //  Option<FxThumb> thumbActorDmm = this.scrapeActorThumbFromDmm(actressName);

                    System.out.println("scrapeActors() >> " + actressName + " " + actressIDHref);

                    // If thumb exist in DMM use it
                  //  actorList.add(new Actor(actressName, "", thumbActorDmm.getOrNull()));

                });
            }

            CompletableFuture<?>[] futures = tasks.stream()
                    .map(task -> CompletableFuture.runAsync(task, Systems.getExecutorServices()))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
        }
        return actorList;
    }

    @Override
    public void scrapeActorsAsync(ObservableList<ActorV2> observableList) {
        Element actressIDElements = this.document.select("ul[class=performer]").first();

        if (actressIDElements != null) {
            Elements actressURL = actressIDElements.select("a");

            for (Element actressIDLink : actressURL) {
                Systems.useExecutors(() -> {
                    String actressIDHref = actressIDLink.attr("abs:href");
                    String actressName = actressIDLink.text();

                    ActorV2 actor = DmmParsingProfile.scrapeActorThumbFromDmm(actressName);

                    GUICommon.debugMessage("scrapeActorsAsync() >> " + actressName + " " + actressIDHref);

                    Platform.runLater(() -> observableList.add(actor));
                });
            }
        }
    }


    @Override
    public ArrayList<Director> scrapeDirectors() {
        ArrayList<Director> directors = new ArrayList<>();

        Element directorElement = this.document.select("tr th:contains(監督) + td").first();

        if (directorElement != null && directorElement.hasText()) {
            directors.add(new Director(directorElement.text(), null));

            System.out.println("scrapeDirectors() >> " + directorElement.text());
        }

        return directors;
    }

    @Override
    public Option<Studio> scrapeStudio() {
        Element studioElement = this.document.select("tr th:contains(レーベル) + td").first();

        return (studioElement == null) ? Option.none() : Option.of(new Studio(studioElement.text()));
    }

    @Override
    public Studio scrapeMaker() {
        Element studioElement = this.document.select("tr th:contains(メーカー) + td").first();

        if (studioElement != null) {
            System.out.println("scrapeMaker() >> " + studioElement.text());

            return new Studio(studioElement.text());
        } else {
            return Studio.BLANK_STUDIO;
        }
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
    public FxThumb[] scrapeExtraFanart() {
        Elements extraArtElements = this.document.select("ul[class=digestimage] a");

        ArrayList<FxThumb> thumbsList = new ArrayList<>(1 + extraArtElements.size());

        for (Element extraArt : extraArtElements) {
            String thumbsLink = extraArt.attr("abs:href");

            if (thumbsLink != null) {
                System.out.println("scrapeExtraArt() >> " + thumbsLink);
                thumbsList.add(FxThumb.of(thumbsLink).get());
            }
        }

        return thumbsList.toArray(new FxThumb[0]);
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
