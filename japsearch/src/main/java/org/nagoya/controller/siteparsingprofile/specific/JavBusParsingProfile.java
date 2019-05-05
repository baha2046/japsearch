package org.nagoya.controller.siteparsingprofile.specific;

import io.vavr.collection.Stream;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import javafx.application.Platform;
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
import org.nagoya.model.dataitem.Runtime;
import org.nagoya.model.dataitem.*;
import org.nagoya.system.ExecuteSystem;
import org.nagoya.system.Systems;

import java.io.File;
import java.io.IOException;
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
        Element titleElement = this.document.select(JavBusCSSQuery.Q_TITLE).first();
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
    public Option<Plot> scrapePlot() {
        return Option.none();
    }

    @Override
    public Option<Runtime> scrapeRuntime() {
        String lengthWord = (this.scrapingLanguage == Language.ENGLISH) ? "Length:" : "収録時間:";
        Element lengthElement = this.document.select("p:contains(" + lengthWord + ")").first();
        if (lengthElement != null) {
            //Getting rid of the word "min" in both Japanese and English
            String runtimeText = lengthElement.ownText().trim().replace("min", "");
            runtimeText = runtimeText.replace("分", "");
            return Option.of(new Runtime(runtimeText));
        }
        return Option.none();
    }

    @Override
    public Option<FxThumb> scrapeCover() {
        Element posterElement = this.document.select(JavBusCSSQuery.Q_COVER).first();

        if (posterElement != null) {
            return FxThumb.of(posterElement.attr("href"));
        }
        return Option.none();

    }

    @Override
    public Stream<FxThumb> scrapeExtraImage() {
        Elements extraFanartElements = this.document.select(JavBusCSSQuery.Q_THUMBS);

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
    public ID scrapeID() {
        Element idElement = this.document.select(JavBusCSSQuery.Q_ID).first();

        if (idElement != null) {
            return new ID(idElement.text());
        } else {
            return ID.BLANK_ID;
        }
    }

    @Override
    public ArrayList<Genre> scrapeGenres() {
        ArrayList<Genre> genreList = new ArrayList<>();
        Elements genreElements = this.document.select(JavBusCSSQuery.Q_GENRES);
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
    public Future<List<ActorV2>> scrapeActorsAsync() {
        return Future.of(Systems.getExecutorServices(ExecuteSystem.role.NORMAL), () ->
        {
            List<ActorV2> observableList = new ArrayList<>();

            Elements actorElements = this.document.select(JavBusCSSQuery.Q_ACTORS);
            if (actorElements != null) {

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
                    observableList.add(actor);
                }

            }

            return observableList;
        });
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
