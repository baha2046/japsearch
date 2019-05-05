package org.nagoya.controller;

import com.jfoenix.controls.JFXDialog;
import cyclops.control.Try;
import io.vavr.concurrent.Future;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nagoya.GUICommon;
import org.nagoya.controller.siteparsingprofile.SiteParsingProfile;
import org.nagoya.model.MovieV2;
import org.nagoya.model.SearchResult;
import org.nagoya.system.Systems;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ScrapeMovieAction {

    //private static ScrapeMovieAction instance = new ScrapeMovieAction();

    @Contract(pure = true)
    private ScrapeMovieAction() {
    }

    @NotNull
    public static <T, X extends Throwable> Future<T> futureFromTry(@NotNull final Executor ex, final Supplier<Try<T, X>> s) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        ex.execute(() -> s.get().fold(cf::complete, cf::completeExceptionally));
        return Future.fromCompletableFuture(cf);
    }

    /**
     * Run in Background
     * Search for results
     */
    @NotNull
    public static Future<SearchResult[]> scrapeMovie(File file, SiteParsingProfile siteScraper, String searchStr) {
        return futureFromTry(Systems.getExecutorServices(),
                () -> Try.withCatch(() -> MovieV2.scrapeMovie(file, siteScraper, searchStr), IOException.class));
    }

    /**
     * Run in FX Thread
     * Ask user for result selection
     *
     * @param results SearchResult Array
     */
    @NotNull
    public static Future<MovieV2> scrapeMovieDetail(@NotNull SearchResult[] results, SiteParsingProfile siteScraper) {


        CompletableFuture<MovieV2> movieCompletableFuture = new CompletableFuture<>();

        if (results.length == 1) {
            scrapeMovieDetail(results[0], siteScraper, movieCompletableFuture);
            return Future.fromCompletableFuture(movieCompletableFuture);
        }

        ListView<SearchResult> listView = new ListView<>();
        ObservableList<SearchResult> data = FXCollections.observableArrayList(results);

        JFXDialog dialog = GUICommon.getAvailableDialog();

        if (dialog == null) {
            movieCompletableFuture.completeExceptionally(new Exception("No Dialog"));
            return Future.fromCompletableFuture(movieCompletableFuture);
        }

        listView.setMinWidth(530);
        listView.setPrefHeight(400);
        listView.setMaxSize(530, 400);
        listView.setCellFactory((ListView<SearchResult> l) -> new MovieSelectorRectCell());
        listView.setItems(data);
        listView.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {
                dialog.close();
                scrapeMovieDetail(results[listView.getSelectionModel().getSelectedIndex()], siteScraper, movieCompletableFuture);
            }
        });

        GUICommon.showDialog(dialog, "Select : (Double Click on the List)", listView, "Cancel", null, null);

        return Future.fromCompletableFuture(movieCompletableFuture);
    }

    /**
     * Run in Background
     * Get the result data
     */
    public static void scrapeMovieDetail(SearchResult sresult, SiteParsingProfile siteScraper, CompletableFuture<MovieV2> movieCompletableFuture) {

        GUICommon.setLoading(true);

        Systems.useExecutors(() ->
        {
            MovieV2 movie = MovieV2.fromSearchResult(sresult, siteScraper, true);
            movieCompletableFuture.complete(movie);
            GUICommon.setLoading(false);
        });
    }

    /**
     * Run in FX Thread
     * Ask user for overwrite result
     */
  /*  private void scrapeMovieResultCompare(Movie currentMovie, Movie movieToWrite) {
        FXMovieDetailCompareView unit = new FXMovieDetailCompareView();
        unit.setEditable(false);
        unit.setMovieOld(movieDetailDataMap, this.actorObservableList, this.genreObservableList);
        unit.setMovieNew(movieToWrite);

        GUICommon.showDialog("Select the information you want to overwrite :", unit.getPane(), "Cancel", "Confirm", () -> {
            if (!unit.isUseTitle()) {
                movieToWrite.setTitle(currentMovie.getTitle());
            }
            if (!unit.isUseDate()) {
                movieToWrite.setYear(currentMovie.getYear());
                movieToWrite.setReleaseDate(currentMovie.getReleaseDate());
            }
            if (!unit.isUseID()) {
                movieToWrite.setId(currentMovie.getId());
            }
            if (!unit.isUseStudio()) {
                movieToWrite.setStudio(currentMovie.getStudio());
            }
            if (!unit.isUseMaker()) {
                movieToWrite.setMaker(currentMovie.getMaker());
            }
            if (!unit.isUseSet()) {
                movieToWrite.setSet(currentMovie.getSet());
            }
            if (!unit.isUsePlot()) {
                movieToWrite.setPlot(currentMovie.getPlot());
            }
            if (!unit.isUseGenres()) {
                movieToWrite.setGenres(currentMovie.getGenres());
            }
            if (!unit.isUseActors()) {
                movieToWrite.setActors(currentMovie.getActors());
            }
            if (!unit.isUseDirector()) {
                movieToWrite.setDirectors(currentMovie.getDirectors());
            }
            if (!unit.isUseCover()) {
                movieToWrite.setPoster(currentMovie.getPoster());
                movieToWrite.setCover(currentMovie.getCover());
            }
            if (!unit.isUseExtraArt()) {
                movieToWrite.setExtraFanart(currentMovie.getExtraFanart());
            }
        });
    }
*/

    static class MovieSelectorRectCell extends ListCell<SearchResult> {
        @Override
        public void updateItem(SearchResult item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty) {
                this.setText(item.toString());
                this.setPrefWidth(400);
                this.setWrapText(true);

                if (item.getPreviewImage().isDefined()) {
                    //system.out.println("item.getImageIcon() != null");
                    ImageView mvView = new ImageView();
                    item.getPreviewImage().peek(t->t.getImage(mvView::setImage));
                    this.setGraphic(mvView);
                } else {
                    //system.out.println("item.getImageIcon() == null");
                    this.setGraphic(null);
                }
            } else {
                this.setGraphic(null);
                this.setText("");
            }
        }

    }
}
