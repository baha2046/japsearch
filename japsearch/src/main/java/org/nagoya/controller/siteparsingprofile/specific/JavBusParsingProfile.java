package org.nagoya.controller.siteparsingprofile.specific;

import io.vavr.collection.Stream;
import io.vavr.control.Option;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nagoya.controller.languagetranslation.JapaneseCharacter;
import org.nagoya.controller.languagetranslation.Language;
import org.nagoya.controller.languagetranslation.TranslateString;
import org.nagoya.controller.siteparsingprofile.SiteParsingProfile;
import org.nagoya.model.SearchResult;
import org.nagoya.model.dataitem.*;
import org.nagoya.model.dataitem.Runtime;
import org.nagoya.system.Systems;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class JavBusParsingProfile extends SiteParsingProfile implements SpecificProfile {

    public static final String urlLanguageEnglish = "en";
    public static final String urlLanguageJapanese = "ja";
    //JavBus divides movies into two categories - censored and uncensored.
    //All censored movies need cropping of their poster
    private boolean isCensoredSearch = true;
    private Document japaneseDocument;

    public static String parserName() {
        return "JavBus.com";
    }

    @Override
    public List<ScraperGroupName> getScraperGroupNames() {
        if (this.groupNames == null) {
            this.groupNames = Arrays.asList(ScraperGroupName.JAV_CENSORED_SCRAPER_GROUP);
        }
        return this.groupNames;
    }

    private void initializeJapaneseDocument() {
        if (this.japaneseDocument == null) {
            String urlOfCurrentPage = this.document.location();
            if (urlOfCurrentPage != null && urlOfCurrentPage.contains("/en/")) {
                //the genres are only available on the japanese version of the page
                urlOfCurrentPage = urlOfCurrentPage.replaceFirst(Pattern.quote("http://www.javbus.com/en/"), "http://www.javbus.com/ja/");
                if (urlOfCurrentPage.length() > 1) {
                    try {
                        this.japaneseDocument = Jsoup.connect(urlOfCurrentPage).userAgent("Mozilla").ignoreHttpErrors(true).timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).get();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else if (this.document != null) {
                this.japaneseDocument = this.document;
            }
        }
    }

    @Override
    public Title scrapeTitle() {
        Element titleElement = this.document.select("div.container h3").first();
        if (titleElement != null) {
            String titleText = titleElement.text();
            titleText = titleText.replace("- JavBus", "");
            //Remove the ID from the front of the title
            /*if (titleText.contains(" ")) {
                titleText = titleText.substring(titleText.indexOf(" "), titleText.length());
            }*/
            //Translate the element using google translate if needed
            if (this.scrapingLanguage == Language.ENGLISH && JapaneseCharacter.containsJapaneseLetter(titleText)) {
                titleText = TranslateString.translateStringJapaneseToEnglish(titleText);
            }
            return new Title(titleText);
        } else {
            return new Title("");
        }
    }


    @Override
    public Option<Set> scrapeSet() {
        String seriesWord = (this.scrapingLanguage == Language.ENGLISH) ? "Series:" : "シリーズ:";
        Element setElement = this.document.select("span.header:containsOwn(" + seriesWord + ") ~ a").first();
        return (null == setElement) ? Option.none() : Option.of(new Set(setElement.text()));
    }


    @Override
    public Option<Year> scrapeYear() {
        return this.scrapeReleaseDate().map(ReleaseDate::getYear);
    }

    @Override
    public Option<ReleaseDate> scrapeReleaseDate() {
        String releaseDateWord = (this.scrapingLanguage == Language.ENGLISH) ? "Release Date:" : "発売日:";
        Element releaseDateElement = this.document.select("p:contains(" + releaseDateWord + ")").first();
        if (releaseDateElement != null && releaseDateElement.ownText().trim().length() > 4) {
            String releaseDateText = releaseDateElement.ownText().trim();
            return Option.of(new ReleaseDate(releaseDateText));
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
        return Outline.BLANK_OUTLINE;
    }

    @Override
    public Option<Plot> scrapePlot() {
        return Option.none();
    }

    @Override
    public Tagline scrapeTagline() {
        return Tagline.BLANK_TAGLINE;
    }

    @Override
    public Option<Runtime> scrapeRuntime() {
        String lengthWord = (this.scrapingLanguage == Language.ENGLISH) ? "Length:" : "収録時間:";
        Element lengthElement = this.document.select("p:contains(" + lengthWord + ")").first();
        if (lengthElement != null && lengthElement.ownText().trim().length() >= 0) {
            //Getting rid of the word "min" in both Japanese and English
            String runtimeText = lengthElement.ownText().trim().replace("min", "");
            runtimeText = runtimeText.replace("分", "");
            return Option.of(new Runtime(runtimeText));
        }
        return Option.none();
    }

    @Deprecated
    @Override
    public FxThumb[] scrapePosters() {
        return this.scrapePostersAndFanart(true);
    }

    @Deprecated
    @Override
    public FxThumb[] scrapeFanart() {
        return this.scrapePostersAndFanart(false);
    }

    @Override
    public Option<FxThumb> scrapeCover() {
        Element posterElement = this.document.select("a.bigImage").first();
        System.out.println("updateView  -- scrapeID " + posterElement.toString());
        if (posterElement != null) {

            return FxThumb.of(posterElement.attr("href"));

        }
        return Option.none();

    }

    @Deprecated
    private FxThumb[] scrapePostersAndFanart(boolean isPosterScrape) {
        Element posterElement = this.document.select("a.bigImage").first();
        if (posterElement != null) {
            FxThumb posterImage = FxThumb.of(posterElement.attr("href")).get();
            return new FxThumb[]{posterImage};
        } else {
            return new FxThumb[0];
        }
    }

    @Override
    @Deprecated
    public FxThumb[] scrapeExtraFanart() {
        Elements extraFanartElements = this.document.select("a.sample-box");
        if (extraFanartElements != null && extraFanartElements.size() > 0) {
            FxThumb[] extraFanart = new FxThumb[extraFanartElements.size()];
            int i = 0;
            for (Element extraFanartElement : extraFanartElements) {
                String href = extraFanartElement.attr("href");
                extraFanart[i] = FxThumb.of(href).get();
                i++;
            }
            return extraFanart;
        }
        return new FxThumb[0];
    }

    @Override
    public Stream<FxThumb> scrapeExtraImage() {
        Elements extraFanartElements = this.document.select("a.sample-box");

        if (extraFanartElements == null || extraFanartElements.size() > 20) {
            return Stream.empty();
        }

        return Stream.ofAll(extraFanartElements.stream())
                .map((ex) -> ex.attr("href"))
                .filter(Objects::nonNull)
                .peek((link) -> System.out.println("scrapeExtraArt() >> " + link))
                .map(FxThumb::of)
                .flatMap(Option::toStream);
    }

    @Override
    public Option<MPAARating> scrapeMPAA() {
        return Option.of(MPAARating.RATING_XXX);
    }

    @Override
    public ID scrapeID() {
        Element idElement = this.document.select("p:contains(品番:) > span + span").first();
        System.out.println("updateView  -- scrapeID " + idElement.toString());
        if (idElement != null) {
            return new ID(idElement.text());
        } else {
            return ID.BLANK_ID;
        }
    }

    @Override
    public ArrayList<Genre> scrapeGenres() {
        ArrayList<Genre> genreList = new ArrayList<>();
        Elements genreElements = this.document.select("span.genre a[href*=/genre/]");
        if (genreElements != null) {
            for (Element genreElement : genreElements) {
                String genreText = genreElement.text();
                if (genreElement.text().length() > 0) {
                    //some genre elements are untranslated, even on the english site, so we need to do it ourselves
                    if (this.scrapingLanguage == Language.ENGLISH && JapaneseCharacter.containsJapaneseLetter(genreText)) {
                        genreText = TranslateString.translateStringJapaneseToEnglish(genreText);
                    }
                    genreList.add(new Genre(WordUtils.capitalize(genreText)));
                }
            }
        }
        return genreList;
    }

    @Override
    public ArrayList<Actor> scrapeActors() {
        ArrayList<Actor> actorList = new ArrayList<>();
        Elements actorElements = this.document.select("div.star-box li a img");
        if (actorElements != null) {
            for (Element currentActor : actorElements) {
                FxThumb thumbnail = null;
                String actorName = currentActor.attr("title");
                //Sometimes for whatever reason the english page still has the name in japanaese, so I will translate it myself
                if (this.scrapingLanguage == Language.ENGLISH && JapaneseCharacter.containsJapaneseLetter(actorName)) {
                    actorName = TranslateString.translateJapanesePersonNameToRomaji(actorName);
                }
                String actorImage = currentActor.attr("src");
                if (actorImage != null && !actorImage.contains("printing.gif") && fileExistsAtURL(actorImage)) {

                    try {
                        thumbnail = FxThumb.of(new URL(actorImage));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                actorList.add(new Actor(actorName, null, thumbnail));
            }
        }
        return actorList;
    }

    @Override
    public void scrapeActorsAsync(ObservableList<ActorV2> observableList) {
        Elements actorElements = this.document.select("div.star-box li a img");
        if (actorElements != null) {
            Systems.useExecutors(() -> {
                for (Element currentActor : actorElements) {
                    String actorName = currentActor.attr("title");
                    //Sometimes for whatever reason the english page still has the name in japanaese, so I will translate it myself
                    if (this.scrapingLanguage == Language.ENGLISH && JapaneseCharacter.containsJapaneseLetter(actorName)) {
                        actorName = TranslateString.translateJapanesePersonNameToRomaji(actorName);
                    }

                    String actorImage = currentActor.attr("src");

                    if (actorImage == null || actorImage.contains("printing.gif") || !fileExistsAtURL(actorImage)) {
                        actorImage = "";
                    }

                    ActorV2 actor = ActorV2.of(actorName, ActorV2.Source.JAVBUS, "", actorImage, "");
                    Platform.runLater(() -> observableList.add(actor));
                }
            });
        }
    }

    @Override
    public ArrayList<Director> scrapeDirectors() {
        ArrayList<Director> directorList = new ArrayList<>();
        String directorWord = (this.scrapingLanguage == Language.ENGLISH) ? "Director:" : "監督:";
        Element directorElement = this.document.select("span.header:containsOwn(" + directorWord + ") ~ a").first();
        if (directorElement != null && directorElement.text().length() > 0) {
            directorList.add(new Director(directorElement.text(), null));
        }
        return directorList;
    }

    @Override
    public Option<Studio> scrapeStudio() {
        String studioWord = (this.scrapingLanguage == Language.ENGLISH) ? "Studio:" : "レーベル:";
        Element studioElement = this.document.select("span.header:containsOwn(" + studioWord + ") ~ a").first();

        return (studioElement == null) ? Option.none() : Option.of(new Studio(studioElement.text()));
    }

    @Override
    public Studio scrapeMaker() {
        String makerWord = (this.scrapingLanguage == Language.ENGLISH) ? "Studio:" : "メーカー:";
        Element makerElement = this.document.select("span.header:containsOwn(" + makerWord + ") ~ a").first();

        return (makerElement == null) ? Studio.BLANK_STUDIO : new Studio(makerElement.text());
    }


    @Override
    public String createSearchString(File file, String searchStr) {
        this.scrapedMovieFile = file;

        //system.out.println("fileNameNoExtension in DMM: " + fileNameNoExtension);
        URLCodec codec = new URLCodec();
        try {
            return "http://www.javbus.com/" + this.getUrlLanguageToUse() + "/search/" + codec.encode(searchStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUrlLanguageToUse() {
        String urlLanguageToUse = (this.scrapingLanguage == Language.ENGLISH) ? urlLanguageEnglish : urlLanguageJapanese;
        return urlLanguageToUse;
    }

    @Override
    public SearchResult[] getSearchResults(String searchString) throws IOException {
        ArrayList<SearchResult> linksList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(searchString).userAgent("Mozilla").ignoreHttpErrors(true).timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).get();
            Elements videoLinksElements = doc.select("div.item");
            String secondPage = searchString;
            if (videoLinksElements == null || videoLinksElements.size() == 0) {
                secondPage = searchString.replace("/search/", "/uncensored/search/");
                this.isCensoredSearch = false;
            }
            doc = Jsoup.connect(secondPage).userAgent("Mozilla").ignoreHttpErrors(true).timeout(SiteParsingProfile.CONNECTION_TIMEOUT_VALUE).get();
            videoLinksElements = doc.select("div.item");
            if (videoLinksElements != null) {
                for (Element videoLink : videoLinksElements) {
                    String currentLink = videoLink.select("a").attr("href");
                    String currentLinkLabel = videoLink.select("a").text().trim();
                    String currentLinkImage = videoLink.select("img").attr("src");
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
    public SiteParsingProfile newInstance() {
        return new JavBusParsingProfile();
    }

    @Override
    public String getParserName() {
        return parserName();
    }

}
