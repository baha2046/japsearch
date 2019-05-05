package org.nagoya.model;

import io.vavr.collection.Stream;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javafx.collections.ObservableList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.nagoya.GUICommon;
import org.nagoya.controller.siteparsingprofile.SiteParsingProfile;
import org.nagoya.model.dataitem.Runtime;
import org.nagoya.model.dataitem.*;
import org.nagoya.model.xmlserialization.KodiXmlMovieBean;
import org.nagoya.preferences.GeneralSettings;
import org.nagoya.preferences.RenameSettings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MovieV2 {

    private ID movieID;
    private Title movieTitle;
    private Studio movieMaker;

    private Option<ReleaseDate> releaseDates = Option.none();
    private Option<Outline> outlines = Option.none();
    private Option<Studio> studios = Option.none();
    private Option<Set> sets = Option.none();
    private Option<Year> years = Option.none();
    private Option<MPAARating> mpaaRatings = Option.none();
    private Option<Plot> plots = Option.none();
    private Option<Runtime> runtimes = Option.none();
    private Option<Trailer> trailers = Option.none();

    private List<Genre> genreList = new ArrayList<>();
    private List<Director> directorList = new ArrayList<>();
    private Future<List<ActorV2>> actorList = Future.of(ArrayList::new);//FXCollections.observableArrayList();

    private Option<FxThumb> imgFrontCover = Option.none();
    private Option<FxThumb> imgBackCover = Option.none();
    private Stream<FxThumb> imgExtras = Stream.empty();


    @NotNull
    @Contract("_, _, _ -> new")
    public static MovieV2 of(String strID, String strTitle, String strMaker) {
        return new MovieV2(strID, strTitle, strMaker);
    }


    public static Option<MovieV2> fromNfoFile(Path nfoPath) {
        return Try.withResources(() -> Files.lines(nfoPath))
                .of(stream -> stream.collect(Collectors.joining("\n")))
                .map(MovieV2::fixXMLStringFront)
                .map(KodiXmlMovieBean::makeFromXML)
                .filter(Objects::nonNull)
                //.peek(k -> GUICommon.debugMessage("MovieV2 fromNfoFile to movie "))
                .map(KodiXmlMovieBean::toMovie)
                .toOption();
    }

    private static String fixXMLStringFront(String xmlStr) {
        if (xmlStr.contains("<?xml")) {
            while (xmlStr.length() > 0 && !xmlStr.startsWith("<?xml")) {
                if (xmlStr.length() > 1) {
                    xmlStr = xmlStr.substring(1, xmlStr.length());
                } else {
                    break;
                }
            }
        }
        return xmlStr;
    }

    @NotNull
    @Contract("_, _, _ -> new")
    public static MovieV2 fromSearchResult(SearchResult searchResultToUse, SiteParsingProfile siteToParseFrom, boolean getExtra) {

        Document searchMatch = SiteParsingProfile.downloadDocument(searchResultToUse);
        siteToParseFrom.setDocument(searchMatch);

        return new MovieV2(siteToParseFrom, getExtra);
    }

    public static SearchResult[] scrapeMovie(File movieFile, SiteParsingProfile siteToParseFrom, String userSearchString) throws IOException {

        //If the user manually canceled the results on this scraper in a dialog box, just return a null movie
        if (siteToParseFrom.getDiscardResults()) {
            return null;
        }

        String searchString;
        if (userSearchString.equals("")) {
            searchString = siteToParseFrom.createSearchString(movieFile);
        } else {
            searchString = siteToParseFrom.createSearchString(movieFile, userSearchString);
        }

        SearchResult[] searchResults;

        //no URL was passed in so we gotta figure it ourselves
        searchResults = siteToParseFrom.getSearchResults(searchString);
        int levDistanceOfCurrentMatch = 999999; // just some super high number
        String idFromMovieFile = SiteParsingProfile.findIDTagFromFile(movieFile, siteToParseFrom.isFirstWordOfFileIsID());

        System.out.println("Movie scrapeMovie -----------------------------");
        System.out.println("Movie scrapeMovie " + searchResults.length);
        System.out.println("Movie scrapeMovie " + siteToParseFrom.toString());
        System.out.println("Movie scrapeMovie -----------------------------");


        int searchResultNumberToUse = 0;
        //loop through search results and see if URL happens to contain ID number in the URL. This will improve accuracy!
        for (int i = 0; i < searchResults.length; i++) {
            String urltoMatch = searchResults[i].getUrlPath().toLowerCase();

            System.out.println("Movie scrapeMovie Movie " + urltoMatch);

            String idFromMovieFileToMatch = idFromMovieFile.toLowerCase().replaceAll("-", "");
            //system.out.println("Comparing " + searchResults[i].toLowerCase() + " to " + idFromMovieFile.toLowerCase().replaceAll("-", ""));
            if (urltoMatch.contains(idFromMovieFileToMatch)) {
                //let's do some fuzzy logic searching to try to of the "best" match in case we got some that are pretty close
                //and update the variables accordingly so we know what our best match so far is
                int candidateLevDistanceOfCurrentMatch =
                        LevenshteinDistance.getDefaultInstance().apply(urltoMatch.toLowerCase(), idFromMovieFileToMatch);
                if (candidateLevDistanceOfCurrentMatch < levDistanceOfCurrentMatch) {
                    levDistanceOfCurrentMatch = candidateLevDistanceOfCurrentMatch;
                    searchResultNumberToUse = i;
                }
            }
        }

        if (searchResultNumberToUse != 0) {
            SearchResult sr = searchResults[0];
            searchResults[0] = searchResults[searchResultNumberToUse];
            searchResults[searchResultNumberToUse] = sr;
        }

        return searchResults;
    }

    //returns the movie file path without anything like CD1, Disc A, etc and also gets rid of the file extension
    //Example: MyMovie ABC-123 CD1.avi returns MyMovie ABC-123
    //Example2: MyMovie ABC-123.avi returns MyMovie ABC-123
    public static String getUnstackedMovieName(String file) {
        String fileName = file;
        fileName = FilenameUtils.removeExtension(fileName);
        fileName = SiteParsingProfile.stripDiscNumber(fileName);
        //fileName = replaceLast(fileName, file.getName(), SiteParsingProfile.stripDiscNumber(fileName));
        return fileName;
    }

    public static String getUnstackedMovieName(@NotNull Path file) {
        return getUnstackedMovieName(file.getFileName().toString());
    }

    public static String getUnstackedMovieName(@NotNull File file) {
        return getUnstackedMovieName(file.getName());
    }


    public static String getFileNameOfTrailer(File selectedValue) {
        //sometimes the trailer has a different extension
        //than the movie so we will try to brute force a find by trying all movie name extensions
      /*  for (String extension : MovieFilenameFilter.acceptedMovieExtensions) {
            String potentialTrailer = tryToFindActualTrailerHelper(selectedValue, "." + extension);
            if (potentialTrailer != null) {
                return potentialTrailer;
            }
        }
        return getTargetFilePath(selectedValue, "-trailer.mp4");*/
        return "";
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public static String[] getSupportRenameElement() {
        return new String[]{"[", "]", "(", ")", "space", "#id", "#moviename", "#year", "#date", "#studio", "#maker"};
    }

    public static String getFormatUnit(MovieV2 movie, @NotNull String input) {
        String string = "";

        switch (input) {
            case "[":
            case "]":
            case "(":
            case ")":
                string = input;
                break;
            case "space":
                string = " ";
                break;
            case "#id":
                string = movie.getMovieID().getId();
                break;
            case "#moviename":
                string = movie.getMovieTitle().getTitle();
                string = string.replace("...", "");
                string = string.replace("/", "／");
                string = string.replace("?", "？");
                string = string.replace("!", "！");
                if (string.length() > 62) {
                    string = string.substring(0, 62);
                }
                break;
            case "#year":
                string = movie.getYears().map(Year::getYear).getOrElse("");
                break;
            case "#date":
                string = movie.getReleaseDates().map(ReleaseDate::getReleaseDate).getOrElse("");
                break;
            case "#studio":
                string = movie.getStudios().map(Studio::getStudio).getOrElse("");
                break;
            case "#maker":
                string = movie.getMovieMaker().getStudio();
                break;
        }

        return string;
    }


    private MovieV2(String strID, String strTitle, String strMaker) {
        this.setMovieID(new ID(strID));
        this.setMovieTitle(new Title(strTitle));
        this.setMovieMaker(new Studio(strMaker));
    }

    public MovieV2(ArrayList<ActorV2> actors, ArrayList<Director> directors, Stream<FxThumb> extraArt,
                   ArrayList<Genre> genres, ArrayList<Tag> tags,
                   ID id, MPAARating mpaa, OriginalTitle originalTitle, Outline outline, Plot plot,
                   Option<FxThumb> backCover, Option<FxThumb> frontCover, Rating rating, ReleaseDate releaseDate,
                   Runtime runtime, Set set, SortTitle sortTitle, Studio studio, Studio maker, Tagline tagline,
                   Title title, Top250 top250, Trailer trailer, Votes votes, Year year) {
        super();

        this.movieID = id;
        this.movieMaker = maker;
        this.movieTitle = title;

        this.setMpaaRatings(mpaa);
        this.setOutlines(outline);
        this.setPlots(plot);
        this.setReleaseDates(releaseDate);
        this.setRuntimes(runtime);
        this.setSets(set);
        this.setStudios(studio);
        this.setOutlines(outline);
        this.setTrailers(trailer);
        this.setYears(year);

        this.setImgFrontCover(frontCover);
        this.setImgBackCover(backCover);

        this.actorList = Future.successful(actors);
        this.directorList = directors;
        this.imgExtras = extraArt;
        this.genreList = genres;
    }

    public MovieV2(@NotNull SiteParsingProfile siteToScrapeFrom, boolean getExtra) {

        this.movieID = siteToScrapeFrom.scrapeID();
        this.movieTitle = siteToScrapeFrom.scrapeTitle();
        this.movieMaker = siteToScrapeFrom.scrapeMaker();

        this.sets = siteToScrapeFrom.scrapeSet();
        this.years = siteToScrapeFrom.scrapeYear();
        this.plots = siteToScrapeFrom.scrapePlot();
        this.studios = siteToScrapeFrom.scrapeStudio();
        this.releaseDates = siteToScrapeFrom.scrapeReleaseDate();
        this.runtimes = siteToScrapeFrom.scrapeRuntime();
        this.mpaaRatings = siteToScrapeFrom.scrapeMPAA();

        //this.trailers = siteToScrapeFrom.scrapeTrailer();
        //this.outlines = siteToScrapeFrom.scrapeOutline();

        this.imgFrontCover = Option.none();
        this.imgBackCover = siteToScrapeFrom.scrapeCover();

        this.imgExtras = getExtra ? siteToScrapeFrom.scrapeExtraImage() : Stream.empty();

        this.actorList = siteToScrapeFrom.scrapeActorsAsync();

        this.genreList = siteToScrapeFrom.scrapeGenres();
        this.directorList = siteToScrapeFrom.scrapeDirectors();

        //this.setAllDataItemSources(siteToScrapeFrom);
/*
        if (scraperPreferences.getUseFileNameAsTitle() && this.fileName != null && this.fileName.length() > 0) {
            this.title = new Title(this.fileName);
            this.title.setDataItemSource(new DefaultDataItemSource());
        }
*/
    }


    public void releaseMemory() {
        this.getImgFrontCover().peek(FxThumb::releaseMemory);
        this.getImgBackCover().peek(FxThumb::releaseMemory);
    }

    public boolean hasValidTitle() {
        return (this.getMovieTitle().getTitle().length() > 0);
    }

    public void writeToFile(Path nfoFile,
                            Path frontCoverFile,
                            Path backCoverFile,
                            Path folderImagePath,
                            Path actorImagePath,
                            Path extraImagePath,
                            Path trailerFile,
                            GeneralSettings preferences,
                            Option<ObservableList<String>> outText) throws IOException {
        // Output the movie to XML using XStream and a proxy class to
        // translate things to a format that Kodi expects

        this.useLocalPathForFrontCover(frontCoverFile);
        this.writeNfoFile(nfoFile, outText);

        // save the poster out
        this.writeCoverToFile(frontCoverFile, backCoverFile, folderImagePath, outText);


        //write out the extrafanart, if the preference for it is set
        if (extraImagePath != null && preferences.getExtraFanartScrapingEnabledPreference()) {
            System.out.println("Starting write of extra cover into " + extraImagePath);
            this.writeExtraImages(extraImagePath, outText);
        }

        //write the .actor images, if the preference for it is set
        if (actorImagePath != null) {
            System.out.println("Writing .actor images into " + actorImagePath);
            this.writeActorImages(actorImagePath, outText);
        }

        //write out the trailer, if the preference for it is set
        /*Trailer trailerToWrite = this.getTrailer();
        if (preferences.getWriteTrailerToFile() && trailerToWrite != null && trailerToWrite.getTrailer().length() > 0) {
            trailerToWrite.writeTrailerToFile(trailerFile);
        }*/
    }

    public void useLocalPathForFrontCover(Path pathFrontCover) {
        this.getImgFrontCover().peek(t -> t.overwriteThumbURL(GUICommon.pathToUrl(pathFrontCover)));
    }

    public MovieV2 writeNfoFile(Path pathNfo, Option<ObservableList<String>> outText) {

        if (pathNfo != null) {
            String xml = new KodiXmlMovieBean(this).toXML();

            // add the xml header since xstream doesn't do this
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" + "\n" + xml;
            //system.out.println("Xml I am writing to file: \n" + xml);

            try (BufferedWriter writer = Files.newBufferedWriter(pathNfo, Charset.forName("UTF-8"))) {
                writer.write(xml);
                GUICommon.writeToObList(">> " + pathNfo.getFileName().toString(), outText);
            } catch (IOException ex) {
                ex.printStackTrace();
                GUICommon.writeToObList(">> Error at writing : " + pathNfo.getFileName().toString(), outText);
            }
        }

        return this;
    }

    public MovieV2 writeCoverToFile(Path pathFront, Path pathBack, Path pathFolder, Option<ObservableList<String>> outText) {

        if (pathFront != null) {
            this.getImgFrontCover().peek((t) -> t.writeToFile(pathFront.toFile()));
            GUICommon.writeToObList(">> " + pathFront.getFileName().toString(), outText);
        }

        if (pathBack != null) {
            this.getImgBackCover().peek((t) -> t.writeToFile(pathBack.toFile()));
            GUICommon.writeToObList(">> " + pathBack.getFileName().toString(), outText);
        }

        if (pathFolder != null) {
            this.getImgFrontCover().peek((t) -> t.writeToFile(pathFolder.toFile()));
            GUICommon.writeToObList(">> " + pathFolder.getFileName().toString(), outText);
        }

        return this;
    }

    public MovieV2 writeExtraImages(Path pathExtraImage, Option<ObservableList<String>> outText) {

        if (pathExtraImage != null && this.getImgExtras().length() > 0) {

            Try.withResources(() -> Files.walk(pathExtraImage)).of(s ->
                    s.sorted(Comparator.reverseOrder()).map(Path::toFile).map(File::delete).allMatch(i -> i == true)
            );

            if (Files.notExists(pathExtraImage)) {
                try {
                    Files.createDirectories(pathExtraImage);
                    GUICommon.writeToObList(">> Create Directory : " + pathExtraImage.getFileName().toString(), outText);
                } catch (IOException e) {
                    e.printStackTrace();
                    GUICommon.writeToObList(">> Error at Create Directory : " + pathExtraImage.getFileName().toString(), outText);
                    return this;
                }
            }

            int currentExtraFanartNumber = 0;

            for (FxThumb thumb : this.getImgExtras()
                    .filter((t) -> !t.getThumbURL().toString().startsWith("file"))) {
                Path fileNameToWrite = pathExtraImage.resolve(RenameSettings.getFileNameExtraImage() + currentExtraFanartNumber + ".jpg");

                //no need to overwrite perfectly good extra cover since this stuff doesn't change. this will also save time when rescraping since extra IO isn't done.
                if (!Files.exists(fileNameToWrite)) {
                    GUICommon.writeToObList(">> Writing Extra Image File >> " + fileNameToWrite.getFileName().toString(), outText);
                    thumb.writeUrlToFile(fileNameToWrite.toFile(), true);
                }
                currentExtraFanartNumber++;
            }
        }

        return this;
    }

    public MovieV2 writeActorImages(Path pathActorImage, Option<ObservableList<String>> outText) {

        //Don't create an empty .actors folder with no actors underneath it
        if (this.getActorListBlock().size() > 0 && pathActorImage != null) {

            //FileUtils.forceMkdir(actorFolder.toFile());
            if (Files.notExists(pathActorImage)) {
                try {
                    Files.createDirectories(pathActorImage);
                    GUICommon.writeToObList(">> Create Directory : " + pathActorImage.getFileName().toString(), outText);
                } catch (IOException e) {
                    e.printStackTrace();
                    GUICommon.writeToObList(">> Error at Create Directory : " + pathActorImage.getFileName().toString(), outText);
                    return this;
                }
            }

            try {
                //on windows this new folder should have the hidden attribute; on unix it is already "hidden" by having a . in front of the name
                //if statement needed for Linux checking .actors hidden flag when .actors is a symlink
                if (!Files.isHidden(pathActorImage)) {
                    Boolean hidden = (Boolean) Files.getAttribute(pathActorImage, "dos:hidden", LinkOption.NOFOLLOW_LINKS);
                    if (hidden != null && !hidden) {
                        try {
                            Files.setAttribute(pathActorImage, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
                        } catch (AccessDeniedException e) {
                            System.err.println("I was not allowed to make .actors folder hidden. This is not a big deal - continuing with write of actor files...");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (ActorV2 currentActor : this.getActorListBlock()) {
                String currentActorToFileName = currentActor.getName().replace(' ', '_');
                Path fileNameToWrite = pathActorImage.resolve(currentActorToFileName + ".jpg");

                //System.out.println(">> Writing Actors File >>> " + fileNameToWrite.toString());
                //File fileNameToWrite = new File(actorFolder.getPath() + File.separator + currentActorToFileName + ".jpg");
                try {
                    currentActor.writeImageToFile(fileNameToWrite.toFile());
                    GUICommon.writeToObList(">> Writing Actors File >> " + fileNameToWrite.getFileName().toString(), outText);
                } catch (IOException e) {
                    e.printStackTrace();
                    GUICommon.writeToObList(">> Error Writing Actors File >> " + fileNameToWrite.getFileName().toString(), outText);
                }
            }

        }

        return this;
    }


    public ID getMovieID() {
        return this.movieID;
    }

    public void setMovieID(ID movieID) {
        this.movieID = movieID;
    }

    public Title getMovieTitle() {
        return this.movieTitle;
    }

    public void setMovieTitle(Title movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Studio getMovieMaker() {
        return this.movieMaker;
    }

    public void setMovieMaker(Studio movieMaker) {
        this.movieMaker = movieMaker;
    }

    public Option<ReleaseDate> getReleaseDates() {
        return this.releaseDates;
    }

    public void setReleaseDates(ReleaseDate releaseDates) {
        this.releaseDates = Option.of(releaseDates);
    }

    public Option<Outline> getOutlines() {
        return this.outlines;
    }

    public void setOutlines(Outline outlines) {
        this.outlines = Option.of(outlines);
    }

    public Option<Studio> getStudios() {
        return this.studios;
    }

    public void setStudios(Studio studios) {
        this.studios = Option.of(studios);
    }

    public Option<Set> getSets() {
        return this.sets;
    }

    public void setSets(Set sets) {
        this.sets = Option.of(sets);
    }

    public Option<Year> getYears() {
        return this.years;
    }

    public void setYears(Year years) {
        this.years = Option.of(years);
    }

    public Option<MPAARating> getMpaaRatings() {
        return this.mpaaRatings;
    }

    public void setMpaaRatings(MPAARating mpaaRatings) {
        this.mpaaRatings = Option.of(mpaaRatings);
    }

    public Option<Plot> getPlots() {
        return this.plots;
    }

    public void setPlots(Plot plots) {
        this.plots = Option.of(plots);
    }

    public Option<Runtime> getRuntimes() {
        return this.runtimes;
    }

    public void setRuntimes(Runtime runtimes) {
        this.runtimes = Option.of(runtimes);
    }

    public Option<Trailer> getTrailers() {
        return this.trailers;
    }

    public void setTrailers(Trailer trailers) {
        this.trailers = Option.of(trailers);
    }

    public List<Genre> getGenreList() {
        return this.genreList;
    }

    public void setGenreList(List<Genre> genreList) {
        this.genreList = genreList;
    }

    public List<Director> getDirectorList() {
        return this.directorList;
    }

    public void setDirectorList(List<Director> directorList) {
        this.directorList = directorList;
    }

    public void getActorList(Consumer<List<ActorV2>> c) {
        this.actorList.peek(c);
    }

    public List<ActorV2> getActorListBlock() {
        return this.actorList.get();
    }

    public void setActorList(List<ActorV2> actorList) {
        this.actorList = Future.successful(actorList);
    }

    public Option<FxThumb> getImgFrontCover() {
        return this.imgFrontCover;
    }

    public void setImgFrontCover(FxThumb imgFrontCover) {
        this.imgFrontCover = Option.of(imgFrontCover);
    }

    public void setImgFrontCover(Option<FxThumb> imgFrontCover) {
        this.imgFrontCover = imgFrontCover;
    }

    public Option<FxThumb> getImgBackCover() {
        return this.imgBackCover;
    }

    public void setImgBackCover(FxThumb imgBackCover) {
        this.imgBackCover = Option.of(imgBackCover);
    }

    public void setImgBackCover(Option<FxThumb> imgBackCover) {
        this.imgBackCover = imgBackCover;
    }

    public Stream<FxThumb> getImgExtras() {
        return this.imgExtras;
    }

    public void setImgExtras(Stream<FxThumb> imgExtras) {
        this.imgExtras = imgExtras;
    }

    public boolean hasBackCover() {
        return this.imgBackCover.isDefined();
    }

    public boolean hasFrontCover() {
        return this.imgFrontCover.isDefined();
    }

}
