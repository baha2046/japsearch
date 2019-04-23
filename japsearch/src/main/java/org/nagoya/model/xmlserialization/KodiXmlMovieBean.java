package org.nagoya.model.xmlserialization;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import org.nagoya.model.MovieV2;
import org.nagoya.model.dataitem.*;
import org.nagoya.model.dataitem.Runtime;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class which handles serializing a Movie object to and from XML
 */
public class KodiXmlMovieBean {

    private String title;
    private String originaltitle;
    private String sorttitle;
    private String set;
    private String year;
    private String top250;
    private String trailer;
    private String votes;
    private String rating;
    private String outline;
    private String plot;
    private String tagline;
    private String runtime;
    private String releasedate;
    private String studio;
    private String maker;
    private String thumb;
    private KodiXmlFanartBean fanart;
    private String mpaa;
    private String id;
    private String[] genre;
    private String[] tag;
    // private String[] credits; add in later
    private ArrayList<KodiXmlActorBean> actor;
    private String[] director;

    /**
     * Constructor - handles conversion of a Movie object to a KodiXmlMovieBean object.
     * Program preferences are taken into account when performing the object conversion so that, for example,
     * certain fields will not be written to the XML
     *
     * @param movie - Movie to create the KodiXmlMovieBean from
     */
    public KodiXmlMovieBean(MovieV2 movie) {

        this.id = movie.getMovieID().getId();
        this.title = movie.getMovieTitle().getTitle();
        this.maker = movie.getMovieMaker().getStudio();
        this.originaltitle = this.title;
        this.sorttitle = this.title;

        this.set = movie.getSets().map(Set::getSet).getOrElse("");
        this.year = movie.getYears().map(Year::getYear).getOrElse("");
        this.trailer = movie.getTrailers().map(Trailer::getTrailer).getOrElse("");
        this.outline = movie.getOutlines().map(Outline::getOutline).getOrElse("");
        this.plot = movie.getPlots().map(Plot::getPlot).getOrElse("");
        this.runtime = movie.getRuntimes().map(org.nagoya.model.dataitem.Runtime::getRuntime).getOrElse("");
        this.releasedate = movie.getReleaseDates().map(ReleaseDate::getReleaseDate).getOrElse("");
        this.studio = movie.getStudios().map(Studio::getStudio).getOrElse("");
        this.mpaa = movie.getMpaaRatings().map(MPAARating::getMPAARating).getOrElse("");
        this.votes = "";
        this.top250 = "";
        this.tagline = "";
        this.rating = "";

        //Posters
        this.thumb = movie.getImgFrontCover().map(FxThumb::getThumbURL).map(URL::toString).getOrElse("");

        if(movie.hasBackCover())
        {
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
        this.actor = new ArrayList<>(movie.getActorList().size());
        for (ActorV2 currentActor : movie.getActorList()) {
            String imgUrl = currentActor.getNetImage().map(FxThumb::getThumbURL).map(URL::toString).getOrElse("");
            this.actor.add(new KodiXmlActorBean(currentActor.getName(), "", imgUrl));
        }

        // tag
        this.tag = new String[0];
    }

    public static KodiXmlMovieBean makeFromXML(String xml) {
        XStream xstream = KodiXmlMovieBean.getXMLSerializer();
        xstream.ignoreUnknownElements();
        try {
            return (KodiXmlMovieBean) xstream.fromXML(xml);
        } catch (Exception e) {
            System.err.println("File read from nfo is not in Kodi XML format. This movie will not be read in.");
            return null;
        }
    }

    private static XStream getXMLSerializer() {
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xstream.allowTypeHierarchy(Collection.class);

        xstream.omitField(FxThumb.class, "thumbImage");
        xstream.alias("movie", KodiXmlMovieBean.class);
        xstream.alias("thumb", FxThumb.class);
        xstream.alias("actor", Actor.class);
        xstream.alias("actor", KodiXmlActorBean.class);
        xstream.alias("fanart", KodiXmlFanartBean.class);
        xstream.addImplicitCollection(KodiXmlMovieBean.class, "actor");
        //xstream.addImplicitArray(KodiXmlMovieBean.class, "thumb", "thumb");
        xstream.addImplicitArray(KodiXmlMovieBean.class, "director", "director");
        xstream.addImplicitArray(KodiXmlFanartBean.class, "thumb", "thumb");
        xstream.addImplicitArray(KodiXmlMovieBean.class, "genre", "genre");
        xstream.addImplicitArray(KodiXmlMovieBean.class, "tag", "tag");
        return xstream;
    }

    public MovieV2 toMovie() throws IOException {

        Option<FxThumb> frontCover = FxThumb.of(this.thumb);

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
                new ReleaseDate(this.releasedate), new Runtime(this.runtime), new Set(this.set), new SortTitle(this.sorttitle),
                new Studio(this.studio), new Studio(this.maker), new Tagline(this.tagline),
                new Title(this.title), new Top250(this.top250), new Trailer(this.trailer), new Votes(this.votes), new Year(this.year));
    }

    public String toXML() {
        return getXMLSerializer().toXML(this);
    }

}
