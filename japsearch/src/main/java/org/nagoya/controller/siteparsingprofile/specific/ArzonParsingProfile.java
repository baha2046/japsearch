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
import org.nagoya.controller.languagetranslation.TranslateString;
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

public class ArzonParsingProfile extends SiteParsingProfile implements SpecificProfile {
    private boolean doGoogleTranslation;
    private URL refererURL;

    public ArzonParsingProfile() {
        super();
        this.doGoogleTranslation = (this.scrapingLanguage == Language.ENGLISH);
    }

    public ArzonParsingProfile(Document document) {
        super(document);
        this.doGoogleTranslation = (this.scrapingLanguage == Language.ENGLISH);
    }

    /**
     * Default constructor does not define a document, so be careful not to call
     * scrape methods without initializing the document first some other way.
     * This constructor is mostly used for calling createSearchString() and
     * getSearchResults()
     */
    public ArzonParsingProfile(boolean doGoogleTranslation) {
        super();
        this.doGoogleTranslation = doGoogleTranslation;
        if (this.doGoogleTranslation == false) {
            this.setScrapingLanguage(Language.JAPANESE);
        }
    }

    public ArzonParsingProfile(boolean doGoogleTranslation, boolean scrapeTrailers) {
        super();
        this.doGoogleTranslation = doGoogleTranslation;
        if (this.doGoogleTranslation == false) {
            this.setScrapingLanguage(Language.JAPANESE);
        }
    }

    public ArzonParsingProfile(Document document, boolean doGoogleTranslation) {
        super(document);
        this.doGoogleTranslation = doGoogleTranslation;
        if (this.doGoogleTranslation == false) {
            this.setScrapingLanguage(Language.JAPANESE);
        }
    }

    public static String fixUpIDFormatting(String idElementText) {
        // remove useless word
        if (idElementText.contains("廃盤")) {
            idElementText = idElementText.substring(0, idElementText.indexOf('廃') - 1);
        }
        return idElementText;
    }

    public static String parserName() {
        return "arzon.jp";
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
        Element titleElement = this.document.select("h1").first();
        // run a google translate on the japanese title

        if (titleElement != null) {
            System.out.println("scrapeTitle() >> " + titleElement.text());

            if (this.doGoogleTranslation) {
                return new Title(TranslateString.translateStringJapaneseToEnglish(titleElement.text()));
            } else {
                return new Title(titleElement.text());
            }
        } else {
            return new Title("");
        }
    }

    @Override
    public OriginalTitle scrapeOriginalTitle() {
        Element titleElement = this.document.select("h1").first();
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
        Element setElement = this.document.select("tr td:contains(シリーズ：) + td").first();
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
        Element releaseDateElement = this.document.select("tr td:contains(発売日：) + td").first();

        if (releaseDateElement != null) {
            String releaseDate = releaseDateElement.text();
            //we want to convert something like 2015/04/25 to 2015-04-25
            releaseDate = StringUtils.replace(releaseDate, "/", "-");
            releaseDate = releaseDate.substring(0, 10);

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
        Element plotElement = this.document.select("div.item_text").first();

        return (plotElement == null) ? Option.none() : Option.of(new Plot((plotElement.text().substring(5))));
    }

    @Override
    public Tagline scrapeTagline() {
        return Tagline.BLANK_TAGLINE;
    }

    @Override
    public Option<Runtime> scrapeRuntime() {
        String runtime = "";

        Element runtimeElement = this.document.select("tr td:contains(収録時間：) + td").first();

        return (runtimeElement == null) ? Option.none() : Option.of(new Runtime(runtimeElement.text().replaceAll("分", "")));
    }

    @Override
    public Trailer scrapeTrailer() {

        return Trailer.BLANK_TRAILER;
    }

    @Override
    public Option<FxThumb> scrapeCover() {
        Element postersElement = this.document.select("a[data-lightbox=jacket1]").first();

        if (postersElement != null) {
            String posterLink = postersElement.attr("abs:href");
            System.out.println("scrapeCover() >> " + posterLink);

            return FxThumb.of(posterLink);
        }
        return Option.none();
    }

    @Override
    @Deprecated
    public FxThumb[] scrapePosters() {
        return null;
    }

    @Override
    @Deprecated
    public FxThumb[] scrapeExtraFanart() {
        Elements extraArtElements = this.document.select("div[class=detail_img] a[data-lightbox=items]");

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
    public Stream<FxThumb> scrapeExtraImage() {
        Elements extraArtElements = this.document.select("div[class=detail_img] a[data-lightbox=items]");

        if (null == extraArtElements) {
            return Stream.empty();
        }

        return Stream.ofAll(extraArtElements.stream())
                .map((ex) -> ex.attr("abs:href"))
                .filter(Objects::nonNull)
                .peek((link) -> System.out.println("scrapeExtraArt() >> " + link))
                .map(FxThumb::of)
                .flatMap(i -> i);
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
        Element idElement = this.document.select("tr td:contains(品番：) + td").first();
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

        Element genre = this.document.select("tr td:contains(ジャンル：) + td").first();
        Elements genreElements = genre.select("ul");

        // Arzon genres divided into parent and child cat.,
        // if there has a child cat. add the child otherwise add the parent cat.
        ArrayList<String> genresList = new ArrayList<>(genreElements.size());
        for (Element genreElement : genreElements) {
            Element currentElement = genreElement.select("li[class=child]").first();

            if (currentElement != null) {
                String genreStr = currentElement.text();

                if (genreStr.contains("その他")) {
                    genreStr = genreStr.replaceAll("その他", "");
                }

                genresList.add(genreStr);
                genre_num++;
            } else {
                currentElement = genreElement.select("li[class=parent]").first();
                if (currentElement != null) {
                    //system.out.println("scrapeGenres" + currentElement.text());
                    genresList.add(currentElement.text());
                    genre_num++;
                }
            }
        }

        ArrayList<Genre> genresReturn = new ArrayList<>(genre_num);
        for (int x = 0; x < genre_num; x++) {
            if (this.doGoogleTranslation) {
                genresReturn.add(new Genre(TranslateString.translateStringJapaneseToEnglish(genresList.get(x))));
            } else {
                genresReturn.add(new Genre(genresList.get(x)));
            }
        }
        return genresReturn;
    }

    @Deprecated
    @Override
    public ArrayList<Actor> scrapeActors() {
        Element actressIDElements = this.document.select("tr td:contains(AV女優：) + td").first();
        Elements actressURL = actressIDElements.select("a");

        System.out.println("scrapeActors() >> " + actressURL.size());
        List<Runnable> tasks = new ArrayList<>();
        ArrayList<Actor> actorList = new ArrayList<>(actressURL.size());
        for (Element actressIDLink : actressURL) {
            tasks.add(() -> {
                String actressIDHref = actressIDLink.attr("abs:href");
                String actressName = actressIDLink.text();

                //Option<FxThumb> thumbActorDmm = this.scrapeActorThumbFromDmm(actressName);


                System.out.println("scrapeActors() >> " + actressName + " " + actressIDHref);

                // If thumb exist in DMM use it
            //    actorList.add(new Actor(actressName, "", thumbActorDmm.getOrNull()));

	/*			// Try to load from Arzon.jp
				try {
					Document actressPage = Jsoup.connect(actressIDHref + "?t=&agecheck=1&m=all&s=&q=").userAgent("Mozilla").timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).of();

					Element actressThumbnailElement = actressPage.select("table.p_list1 tr td img").first();
					String actressThumbnailPath = actressThumbnailElement.attr("abs:src");

					system.out.println("scrapeActors() >> " + actressThumbnailPath);

					// 27682S.jpg is a blank image for no photo
					if(actressThumbnailPath == null || actressThumbnailPath.contains("27682S.jpg"))
					{
						actorList.add(new Actor(actressName, "", null));
					}
					else
					{
						actorList.add(new Actor(actressName, "", new Thumb(actressThumbnailPath, false, refererURL)));
					}

				} catch (SocketTimeoutException e)
				{
					system.err.println("Cannot download from " + actressIDHref.toString() + ": Socket timed out: " + e.getLocalizedMessage());
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
            });
        }

        CompletableFuture<?>[] futures = tasks.stream()
                .map(task -> CompletableFuture.runAsync(task, Systems.getExecutorServices()))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();

        return actorList;

    }

    @Override
    public void scrapeActorsAsync(ObservableList<ActorV2> observableList) {
        Element actressIDElements = this.document.select("tr td:contains(AV女優：) + td").first();
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

    @Override
    public ArrayList<Director> scrapeDirectors() {
        ArrayList<Director> directors = new ArrayList<>();

        Element directorElement = this.document.select("tr td:contains(監督：) + td").first();

        if (directorElement != null && directorElement.hasText()) {
            if (this.doGoogleTranslation) {
                directors.add(new Director(TranslateString.translateStringJapaneseToEnglish(directorElement.text()), null));
            } else {
                directors.add(new Director(directorElement.text(), null));
            }
        }

        System.out.println("scrapeDirectors() >> " + directorElement.text());

        return directors;
    }

    @Override
    public Option<Studio> scrapeStudio() {
        Element studioElement = this.document.select("tr td:contains(AVレーベル：) + td").first();

        return (studioElement == null) ? Option.none() : Option.of(new Studio(studioElement.text()));
    }

    @Override
    public Studio scrapeMaker() {
        Element studioElement = this.document.select("tr td:contains(AVメーカー：) + td").first();

        if (studioElement != null) {
            System.out.println("scrapeStudio() >> " + studioElement.text());

            if (this.doGoogleTranslation) {
                return new Studio(TranslateString.translateStringJapaneseToEnglish(studioElement.text()));
            } else {
                return new Studio(studioElement.text());
            }
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
            this.refererURL = new URL("https://www.arzon.jp/itemlist.html?t=&agecheck=1&m=all&s=&q=" + fileNameURLEncoded);
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

            Elements videoLinksElements = doc.select("dl.hentry");
            if (videoLinksElements != null) {

                for (Element videoLink : videoLinksElements) {
                    String currentLink = videoLink.select("a").attr("abs:href");
                    String currentLinkLabel = videoLink.select("a").attr("title");
                    String currentLinkImage = videoLink.select("img").attr("abs:src");

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
        return "arzon.jp";
    }

    @Override
    public SiteParsingProfile newInstance() {
        GeneralSettings preferences = Systems.getPreferences();
        return new ArzonParsingProfile(!preferences.getScrapeInJapanese());
    }

    @Override
    public String getParserName() {
        return parserName();
    }

}
