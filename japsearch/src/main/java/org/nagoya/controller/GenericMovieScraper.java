package org.nagoya.controller;

import org.nagoya.controller.siteparsingprofile.SiteParsingProfile;
import org.nagoya.model.MovieV2;

public class GenericMovieScraper extends AbstractMovieScraper {

    protected SiteParsingProfile profile;

    public GenericMovieScraper(SiteParsingProfile spp) {
        this.profile = spp;
    }

    @Override
    public MovieV2 createMovie() {
        return new MovieV2(this.profile, true);
    }

}
