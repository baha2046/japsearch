package org.nagoya.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.Contract;
import org.nagoya.GUICommon;
import org.nagoya.controller.siteparsingprofile.SiteParsingProfile;
import org.nagoya.controller.siteparsingprofile.specific.ArzonParsingProfile;
import org.nagoya.controller.siteparsingprofile.specific.DmmParsingProfile;
import org.nagoya.controller.siteparsingprofile.specific.DugaParsingProfile;
import org.nagoya.controller.siteparsingprofile.specific.JavBusParsingProfile;
import org.nagoya.model.DirectoryEntry;
import org.nagoya.model.MovieV2;
import org.nagoya.model.SearchResult;
import org.nagoya.model.dataitem.*;
import org.nagoya.system.FXContextImp;
import org.nagoya.system.MovieLock;
import org.nagoya.system.Systems;
import org.nagoya.system.event.CustomEvent;
import org.nagoya.system.event.CustomEventType;
import org.nagoya.view.FXMovieDetailCompareView;
import org.nagoya.view.FXMoviePanelView;
import org.nagoya.view.dialog.FXImageViewer;
import org.nagoya.view.editor.FXRenameFormatEditor;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FXMoviePanelControl extends FXContextImp {

    public static final CustomEventType EVENT_MOVIE_CHANGE = new CustomEventType("EVENT_MOVIE_CHANGE");

    private static FXMoviePanelControl instance = null;

    private final FXMoviePanelView moviePanel;
    private DirectoryEntry selectedEntry;
    private MovieV2 currentMovie;
    private ObservableList<ActorV2> actorObservableList;
    private final ObservableList<Genre> genreObservableList;
    private final java.util.Map<String, SimpleStringProperty> movieDetailDataMap;

    private final Map<String, SearchResultHandler> searchResultMap;

    private FXMoviePanelControl() {
        this.currentMovie = null;
        this.movieDetailDataMap = new LinkedHashMap<>();

        this.moviePanel = GUICommon.loadFXMLController(FXMoviePanelView.class);
        if (this.moviePanel == null) {
            throw new RuntimeException();
        }
        this.moviePanel.setController(this);

        final String[] field = {"Title", "ID", "Year", "Date", "Set", "Studio", "Maker", "Plot", "Director"};
        for (String i : field) {
            this.movieDetailDataMap.put(i, new SimpleStringProperty(""));
        }

        this.genreObservableList = FXCollections.observableArrayList();
        this.moviePanel.setData(this.movieDetailDataMap, this.actorObservableList, this.genreObservableList);

        this.searchResultMap = HashMap.ofEntries(
                Map.entry(DmmParsingProfile.parserName(), new SearchResultHandler(new DmmParsingProfile(), this.moviePanel.btnSDmm)),
                Map.entry(ArzonParsingProfile.parserName(), new SearchResultHandler(new ArzonParsingProfile(), this.moviePanel.btnSArzon)),
                Map.entry(DugaParsingProfile.parserName(), new SearchResultHandler(new DugaParsingProfile(), this.moviePanel.btnSDuga)),
                Map.entry(JavBusParsingProfile.parserName(), new SearchResultHandler(new JavBusParsingProfile(), this.moviePanel.btnSJavBus))
        );
        this.searchResultMap.forEach((k, b) -> b.disableButton());
    }

    public static FXMoviePanelControl getInstance() {
        if (null == instance) {
            instance = new FXMoviePanelControl();
        }
        return instance;
    }

    @Contract("null -> !null")
    private static String normalize(String str) {
        if (str == null) {
            return "";
        }
        return Normalizer.normalize(str, Normalizer.Form.NFKC);
    }

    /**
     * @param selected Show this movies detail
     */
    public void doMovieChange(DirectoryEntry selected) {

        MovieV2 movie = null;

        this.selectedEntry = selected;

        if (selected != null) {
            if (this.selectedEntry.hasNfo() && MovieLock.getInstance().notInList(this.selectedEntry.getFilePath())) {
                movie = this.selectedEntry.getMovieData();
            }
        }

        this.searchResultMap.forEach((k, v) -> {
            v.disableButton();
            if (v.getSearchResults() != null) {
                v.getSearchResults().cancel(true);
            }
        });
        this.moviePanel.runChangeEffect(movie);
    }

    private final WritableImage EmytyImage = new WritableImage(10, 270);

    public void updateMovieDetail(MovieV2 movie) {

        this.currentMovie = movie;
        this.fireEvent(EVENT_MOVIE_CHANGE, this.currentMovie);

        this.genreObservableList.clear();
        this.moviePanel.btnExtraFanArt.setDisable(true);

        if (this.currentMovie == null) {
            this.moviePanel.setImage(this.EmytyImage, null, this.EmytyImage);
            this.moviePanel.btnSaveMovie.setDisable(true);
            for (String i : this.movieDetailDataMap.keySet()) {
                this.movieDetailDataMap.get(i).setValue("");
            }
        } else {
            this.moviePanel.btnSaveMovie.setDisable(false);

            this.movieDetailDataMap.get("Date").setValue(movie.getReleaseDates().map(ReleaseDate::getReleaseDate).getOrElse(""));
            this.movieDetailDataMap.get("ID").setValue(movie.getMovieID().getId());
            this.movieDetailDataMap.get("Title").setValue(normalize(movie.getMovieTitle().getTitle()));
            this.movieDetailDataMap.get("Plot").setValue(normalize(movie.getPlots().map(Plot::getPlot).getOrElse("")));
            this.movieDetailDataMap.get("Set").setValue(normalize(movie.getSets().map(Set::getSet).getOrElse("")));
            this.movieDetailDataMap.get("Studio").setValue(normalize(movie.getStudios().map(Studio::getStudio).getOrElse("")));
            this.movieDetailDataMap.get("Maker").setValue(normalize(movie.getMovieMaker().getStudio()));
            this.movieDetailDataMap.get("Year").setValue(normalize(movie.getYears().map(Year::getYear).getOrElse("")));

            if (movie.getDirectorList().size() > 0) {
                this.movieDetailDataMap.get("Director").setValue(normalize(movie.getDirectorList().get(0).getName()));
            } else {
                this.movieDetailDataMap.get("Director").setValue("");
            }

            if (movie.getImgFrontCover().isDefined()) {
                this.moviePanel.setImage(
                        movie.getImgFrontCover().map(FxThumb::getImage).getOrElse(this.EmytyImage),
                        null,
                        movie.getImgBackCover().map(FxThumb::getImage).getOrElse(this.EmytyImage)
                );
            } else {
                Image image = movie.getImgBackCover().map(FxThumb::getImage).getOrElse(this.EmytyImage);
                Image cover = this.moviePanel.setImage(
                        image,
                        FxThumb.getCoverCrop(image.getWidth(), image.getHeight(), movie.getMovieID().getId()),
                        image
                );
                this.updatePoster(cover);
            }

            if (movie.getImgExtras().length() > 0) {
                this.moviePanel.btnExtraFanArt.setDisable(false);
            }

            this.actorObservableList = FXCollections.observableArrayList(ActorV2.LOADING_ACTOR);
            movie.getActorList((a) -> Platform.runLater(() -> {
                this.actorObservableList.remove(ActorV2.LOADING_ACTOR);
                this.actorObservableList.addAll(a);
            }));

            //this.tryLoadLocalActorImage(actorImagePath);
            this.moviePanel.setActorList(this.actorObservableList);

            for (Genre genre : movie.getGenreList()) {
                genre.setGenre(normalize(genre.getGenre()));
                this.genreObservableList.add(genre);
            }
        }
    }

    public void displayExtraFanArt() {
        if (this.currentMovie.getImgExtras().length() > 0) {
            FXImageViewer.show(this.currentMovie.getImgExtras());
        }
    }

    private String getButtonStyle() {
        return "-fx-background-color: \n" +
                "        #c3c4c4,\n" +
                "        linear-gradient(#d6d6d6 50%, white 100%),\n" +
                "        radial-gradient(center 50% -40%, radius 200%, #e6e6e6 45%, rgba(230,230,230,0) 50%);\n" +
                "    -fx-background-radius: 30;\n" +
                "    -fx-background-insets: 0,1,1;\n" +
                "    -fx-text-fill: black;\n" +
                "    -fx-font-size: 12;\n" +
                "    -fx-padding: 10 10 10 10;\n" +
                "    -fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 3, 0.0 , 0 , 1 );";
    }

    public void doSaveAction() {
        if (this.currentMovie != null && this.selectedEntry != null && this.selectedEntry.hasMovie()) {
            this.applyModify(this.currentMovie);

            VBox vBox = new VBox();
            vBox.setSpacing(10);

            JFXListView<String> text = new JFXListView<>();
            text.setMinWidth(600);
            text.setMinHeight(500);
            text.setMaxHeight(500);

            JFXButton btnSet = new JFXButton("[ Setting ]");
            btnSet.setButtonType(JFXButton.ButtonType.RAISED);
            btnSet.setOnAction((e) -> FXRenameFormatEditor.show(FXRenameFormatEditor.Type.FILE_NAME));

            JFXButton btnSave = new JFXButton("[ Save ]"/*, new Glyph("FontAwesome", FontAwesome.Glyph.CHECK_CIRCLE)*/);
            btnSave.setButtonType(JFXButton.ButtonType.RAISED);
            btnSave.setOnAction((e) -> this.saveMovieToFile(this.currentMovie, text.getItems()));

            ButtonBar buttonBar = new ButtonBar();
            ButtonBar.setButtonData(btnSet, ButtonBar.ButtonData.HELP);
            ButtonBar.setButtonData(btnSave, ButtonBar.ButtonData.OK_DONE);
            buttonBar.getButtons().setAll(btnSet, btnSave);

            vBox.getChildren().setAll(text, buttonBar);

            GUICommon.showDialog("Save :", vBox, "Close", null, null);
        }
    }

    private void applyModify(MovieV2 movie) {
        if (movie == null) {
            return;
        }

        movie.setMovieID(new ID(this.movieDetailDataMap.get("ID").getValue()));
        movie.setPlots(new Plot(this.movieDetailDataMap.get("Plot").getValue()));
        movie.setSets(new Set(this.movieDetailDataMap.get("Set").getValue()));
        movie.setStudios(new Studio(this.movieDetailDataMap.get("Studio").getValue()));
        movie.setMovieMaker(new Studio(this.movieDetailDataMap.get("Maker").getValue()));
        movie.setMovieTitle(new Title(this.movieDetailDataMap.get("Title").getValue()));
        movie.setYears(new Year(this.movieDetailDataMap.get("Year").getValue()));
        movie.setReleaseDates(new ReleaseDate(this.movieDetailDataMap.get("Date").getValue()));

        movie.getGenreList().clear();
        this.genreObservableList.forEach((genre) -> movie.getGenreList().add(genre));

        this.actorObservableList.remove(ActorV2.LOADING_ACTOR);
        movie.setActorList(new ArrayList<>(this.actorObservableList));
    }

    /**
     * Run in Background
     * Save data to Files
     */
    private void saveMovieToFile(MovieV2 movie, ObservableList<String> outText) {
        GUICommon.setLoading(true);

        this.selectedEntry.setMovieData(movie);
        this.selectedEntry.writeMovieDataToFile(Option.of(outText), () ->
        {
            this.fireEvent(FXFileListControl.EVENT_FOCUS, null);
            GUICommon.setLoading(false);
            //this.fileListPanel.requestFocus();
        });
    }

    public void doScrapeAll(boolean useCustom) {
        if (this.selectedEntry != null && this.selectedEntry.hasMovie()) {

            File file = this.selectedEntry.getDirPath().toFile();

            if (useCustom) {
                this.doScrapeAllCustom();
            } else {
                this.doScrapeMovie(file, "");
            }
        }
    }

    private void doScrapeAllCustom() {
        if (this.selectedEntry != null && this.selectedEntry.hasMovie()) {

            File file = this.selectedEntry.getDirPath().toFile();

            VBox vBox = new VBox();
            HBox hBox = new HBox();
            JFXButton btnUseName = new JFXButton("Use Movie Name");
            JFXButton btnFrontID = new JFXButton("Front Movie ID");
            JFXButton btnBackID = new JFXButton("Back Movie ID");
            JFXTextField txtSearch = new JFXTextField(SiteParsingProfile.findIDTagFromFile(file, Systems.getPreferences().getIsFirstWordOfFileID()));
            txtSearch.setMinWidth(500);
            btnUseName.setOnAction((e) -> txtSearch.setText(file.getName()));
            btnFrontID.setOnAction((e) -> txtSearch.setText(SiteParsingProfile.findIDTagFromFile(file, true)));
            btnBackID.setOnAction((e) -> txtSearch.setText(SiteParsingProfile.findIDTagFromFile(file, false)));
            hBox.setAlignment(Pos.CENTER);
            hBox.setSpacing(30);
            vBox.setSpacing(10);
            hBox.getChildren().addAll(btnUseName, btnFrontID, btnBackID);
            vBox.getChildren().addAll(txtSearch, hBox);

            GUICommon.showDialog("Keyword to search :", vBox, "Cancel", "Search", () -> this.doScrapeMovie(file, txtSearch.getText()));
        }
    }

    private void doScrapeMovie(final File file, final String searchStr) {

        this.searchResultMap.forEach((k, v) -> v.scrapeStart(file, searchStr));

        this.moviePanel.btnSAll.setDisable(true);
        Future<Seq<SearchResult[]>> all = Future.sequence(this.searchResultMap.map(v -> v._2.getSearchResults()));
        all.onComplete((i) -> this.moviePanel.btnSAll.setDisable(false));
    }

    public void doScrapeDetail(String parserName) {
        //GUICommon.getPreferences().setScrapeActor(!this.moviePanel.btnSFast.isSelected());

        this.searchResultMap.get(parserName).get().scrapeDetail()
                .onSuccess(m -> Platform.runLater(() -> this.consumerOfScrapeMovie(m)));
    }

    /**
     * Run in FX Thread
     * Ask user for overwrite result
     */
    private void consumerOfScrapeMovie(MovieV2 movie) {
        if (this.currentMovie == null) {
            this.useScrapeMovieData(movie);
        } else {

            FXMovieDetailCompareView unit = new FXMovieDetailCompareView();
            unit.setEditable(false);
            unit.setMovieOld(this.movieDetailDataMap, this.actorObservableList, this.genreObservableList);
            unit.setMovieNew(movie);


            GUICommon.showDialog("Select the information you want to overwrite :", unit.getPane(), "Cancel", "Confirm", () -> {
                if (!unit.isUseTitle()) {
                    movie.getMovieTitle().setTitle(this.movieDetailDataMap.get("Title").getValue());
                }
                if (!unit.isUseDate()) {
                    movie.setYears(new Year(this.movieDetailDataMap.get("Year").getValue()));
                    movie.setReleaseDates(new ReleaseDate(this.movieDetailDataMap.get("Date").getValue()));
                }
                if (!unit.isUseID()) {
                    movie.getMovieID().setId(this.movieDetailDataMap.get("ID").getValue());
                }
                if (!unit.isUseStudio()) {
                    movie.setStudios(new Studio(this.movieDetailDataMap.get("Studio").getValue()));
                }
                if (!unit.isUseMaker()) {
                    movie.getMovieMaker().setStudio(this.movieDetailDataMap.get("Maker").getValue());
                }
                if (!unit.isUseSet()) {
                    movie.setSets(new Set(this.movieDetailDataMap.get("Set").getValue()));
                }
                if (!unit.isUsePlot()) {
                    movie.setPlots(new Plot(this.movieDetailDataMap.get("Plot").getValue()));
                }
                if (!unit.isUseGenres()) {
                    movie.setGenreList(this.currentMovie.getGenreList());
                }
                if (!unit.isUseActors()) {
                    this.currentMovie.getActorList(movie::setActorList);
                }
                if (!unit.isUseDirector()) {
                    movie.setDirectorList(this.currentMovie.getDirectorList());
                }
                if (!unit.isUseCover()) {
                    movie.setImgFrontCover(this.currentMovie.getImgFrontCover());
                    movie.setImgBackCover(this.currentMovie.getImgBackCover());
                }
                if (!unit.isUseExtraArt()) {
                    movie.setImgExtras(this.currentMovie.getImgExtras());
                }

                this.useScrapeMovieData(movie);
            });

        }
    }

    /* Run in FX Thread
    - Update Result */
    private void useScrapeMovieData(MovieV2 movie) {
        if (this.currentMovie == null) {
            this.moviePanel.runChangeEffect(movie);
        } else {
            this.updateMovieDetail(movie);
        }
    }

    @Override
    public Node getPane() {
        return this.moviePanel.getPane();
    }


    @Override
    public void registerListener() {
        this.registerListener(FXFileListControl.EVENT_LIST_SELECTED);
        this.registerListener(FXArtPanelControl.EVENT_POSTER_CHANGE);
    }

    @Override
    public void executeEvent(CustomEvent e) {
        GUICommon.debugMessage(() -> "executeEvent " + e.getType().getName());

        if (e.getType().equals(FXFileListControl.EVENT_LIST_SELECTED)) {
            this.doMovieChange((DirectoryEntry) e.getObject());
        } else if (e.getType().equals(FXArtPanelControl.EVENT_POSTER_CHANGE)) {
            this.updatePoster((Image) e.getObject());
        }
    }

    private void updatePoster(Image image) {
        GUICommon.debugMessage(() -> "EVENT_POSTER_CHANGE updatePoster");
        if (this.currentMovie.getImgFrontCover().isDefined()) {
            this.currentMovie.getImgFrontCover().peek(t -> t.setImage(image));
        } else {
            FxThumb fxThumb = FxThumb.of();
            fxThumb.setImage(image);
            this.currentMovie.setImgFrontCover(Option.of(fxThumb));
        }
    }


    class SearchResultHandler {
        private final SiteParsingProfile parsingProfile;
        private final JFXButton button;
        private Future<SearchResult[]> searchResults = null;

        @Contract(pure = true)
        @java.beans.ConstructorProperties({"parsingProfile", "button"})
        public SearchResultHandler(SiteParsingProfile parsingProfile, JFXButton button) {
            this.parsingProfile = parsingProfile;
            this.button = button;
        }

        void scrapeStart(File file, String searchStr) {
            this.getButton().setDisable(true);
            this.setSearchResults(ScrapeMovieAction.scrapeMovie(file, this.getParsingProfile(), searchStr));
            this.getSearchResults().onSuccess((r) -> {
                if (r != null && r.length > 0) {
                    this.getButton().setDisable(false);
                }
            });
        }

        Future<MovieV2> scrapeDetail() {
            if (!this.getSearchResults().isSuccess()) {
                return Future.of(() -> {
                    throw new Exception("No Search Result");
                });
            }

            return ScrapeMovieAction.scrapeMovieDetail(this.getSearchResults().get(), this.getParsingProfile());
        }

        void disableButton() {
            this.getButton().setDisable(true);
        }

        public SiteParsingProfile getParsingProfile() {
            return this.parsingProfile;
        }

        public JFXButton getButton() {
            return this.button;
        }

        public Future<SearchResult[]> getSearchResults() {
            return this.searchResults;
        }

        public void setSearchResults(Future<SearchResult[]> searchResults) {
            this.searchResults = searchResults;
        }
    }
}
