package org.nagoya.model.xmlserialization;

import com.thoughtworks.xstream.XStream;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.nagoya.model.MovieV2;
import org.nagoya.model.dataitem.Runtime;
import org.nagoya.model.dataitem.*;
import org.nagoya.preferences.RenameSettings;

import java.net.URL;
import java.util.ArrayList;

/**
 * Class which handles serializing a MovieV2 object to and from XML
 */
public class KodiXmlMovieBean {

    private final String title;
    private final String originaltitle;
    private final String sorttitle;
    private final String rating;
    private final String top250;
    private final String outline;
    private final String plot;
    private final String tagline;
    private final String runtime;
    private final KodiXmlThumbBean thumb[];
    private final String mpaa;
    private final String votes;
    private final KodiXmlFanartBean fanart;
    private final String id;
    private final KodiXmlUniqueidBean uniqueid;
    private final String[] genre;
    private final KodiXmlSetBean set;
    private final String tag;
    private final String[] director;
    private final String premiered;
    private final String year;
    private final String studio;
    private final String maker;
    private final String releasedate;
    private final String trailer;
    // private String[] credits; add in later
    private final ArrayList<KodiXmlActorBean> actor;


    /**
     * Constructor - handles conversion of a MovieV2 object to a KodiXmlMovieBean object.
     * Program preferences are taken into account when performing the object conversion so that, for example,
     * certain fields will not be written to the XML
     *
     * @param movie - MovieV2 to create the KodiXmlMovieBean from
     */
    public KodiXmlMovieBean(MovieV2 movie) {

        this.id = movie.getMovieID().getId();
        this.uniqueid = new KodiXmlUniqueidBean(this.id);

        this.title = movie.getMovieTitle().getTitle();
        this.maker = movie.getMovieMaker().getStudio();
        this.originaltitle = this.title;
        this.sorttitle = "";

        this.set = new KodiXmlSetBean(movie.getSets().map(Set::getSet).getOrElse(""), "");
        this.year = movie.getYears().map(Year::getYear).getOrElse("");
        this.trailer = movie.getTrailers().map(Trailer::getTrailer).getOrElse("");
        //this.outline = movie.getOutlines().map(Outline::getOutline).getOrElse("");
        this.plot = movie.getPlots().map(Plot::getPlot).getOrElse("");
        this.runtime = movie.getRuntimes().map(org.nagoya.model.dataitem.Runtime::getRuntime).getOrElse("");
        this.premiered = movie.getReleaseDates().map(ReleaseDate::getReleaseDate).getOrElse("");
        this.studio = movie.getStudios().map(Studio::getStudio).getOrElse("");
        this.mpaa = movie.getMpaaRatings().map(MPAARating::getMPAARating).getOrElse("");
        this.votes = "";
        this.top250 = "";
        this.tagline = "";
        this.rating = "";
        this.tag = "";

        this.outline = this.plot;
        this.releasedate = this.premiered;

        //Posters
        this.thumb = new KodiXmlThumbBean[2];
        this.thumb[0] = new KodiXmlThumbBean("poster",
                RenameSettings.getFileNameFrontCover(RenameSettings.getSuitableFileName(movie)
                ));
        this.thumb[1] = new KodiXmlThumbBean("landscape",
                movie.getImgBackCover().map(FxThumb::getThumbURL).map(URL::toString).getOrElse(""));

        if (movie.hasBackCover()) {
            FxThumb[] tmpThumbArray = new FxThumb[1];
            tmpThumbArray[0] = movie.getImgBackCover().get();
            this.fanart = new KodiXmlFanartBean(tmpThumbArray);
        } else {
            this.fanart = new KodiXmlFanartBean(new FxThumb[0]);
        }

        // genre
        this.genre = new String[movie.getGenreList().size()];
        for (int i = 0; i < this.genre.length; i++) {
            this.genre[i] = movie.getGenreList().get(i).getGenre();
        }

        // director
        this.director = new String[movie.getDirectorList().size()];
        for (int i = 0; i < this.director.length; i++) {
            this.director[i] = movie.getDirectorList().get(i).getName();
        }

        // actor
        this.actor = new ArrayList<>(movie.getActorListBlock().size());
        for (ActorV2 currentActor : movie.getActorListBlock()) {
            String imgUrl = currentActor.getNetImage().map(FxThumb::getThumbURL).map(URL::toString).getOrElse("");
            this.actor.add(new KodiXmlActorBean(currentActor.getName(), "", imgUrl));
        }

    }

    public static KodiXmlMovieBean makeFromXML(String xml) {
        XStream xstream = XStreamForNfo.getXStream();//KodiXmlMovieBean.getXMLSerializer();

        try {
            return (KodiXmlMovieBean) xstream.fromXML(xml);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("File read from nfo is not in Kodi XML format. This movie will not be read in.");
            return null;
        }
    }

    public MovieV2 toMovie() {

        Option<FxThumb> frontCover = Option.none();
        /*if (this.thumb != null && this.thumb.length >= 1) {
            frontCover = this.thumb[0].toThumb();
        }*/

        Option<FxThumb> backCover = Option.none();
        if (this.fanart != null && this.fanart.getThumb() != null) {
            backCover = FxThumb.of(this.fanart.getThumb()[0]);
        }

        ArrayList<ActorV2> actors = new ArrayList<>();
        if (this.actor != null) {
            actors = new ArrayList<>(this.actor.size());
            for (KodiXmlActorBean currentActor : this.actor) {
                actors.add(currentActor.toActor());
            }
        }

        ArrayList<Genre> genres = new ArrayList<>();
        if (this.genre != null) {
            for (String aGenre : this.genre) {
                genres.add(new Genre(aGenre));
            }
        }

        ArrayList<Tag> tags = new ArrayList<>();

        ArrayList<Director> directors = new ArrayList<>();
        if (this.director != null) {
            directors = new ArrayList<>(this.director.length);
            for (String aDirector : this.director) {
                directors.add(new Director(aDirector, null));
            }
        }


        return new MovieV2(actors, directors, Stream.empty(), genres, tags,
                new ID(this.id), new MPAARating(this.mpaa), new OriginalTitle(this.originaltitle),
                new Outline(this.outline), new Plot(this.plot), backCover, frontCover, new Rating(10, this.rating),
                new ReleaseDate(this.releasedate), new Runtime(this.runtime), this.set.toSet(), new SortTitle(this.sorttitle),
                new Studio(this.studio), new Studio(this.maker), new Tagline(this.tagline),
                new Title(this.title), new Top250(this.top250), new Trailer(this.trailer), new Votes(this.votes), new Year(this.year));
    }

    public String toXML() {
        return XStreamForNfo.getXStream().toXML(this);
    }

}
