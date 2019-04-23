package org.nagoya.model.dataitem;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Trailer extends MovieDataItem {

    public static final Trailer BLANK_TRAILER = new Trailer("");
    private static final long serialVersionUID = -3271900628832584272L;
    private String trailer;

    public Trailer(String trailer) {
        this.setTrailer(trailer);
    }

    public Trailer() {
        this.trailer = "";
    }

    public String getTrailer() {
        return this.trailer;
    }

    public void setTrailer(String trailer) {
        if (trailer == null) {
            this.trailer = "";
        } else {
            this.trailer = trailer;
        }
    }

    @Override
    public String toString() {
        return "Trailer [trailer=\"" + this.trailer + "\"" + this.dataItemSourceToString() + "]";
    }

    public void writeTrailerToFile(Path fileNameToWrite) throws IOException {
        //we don't want to rewrite trailer if the file already exists since that can retrigger a pointlessly long download
        if (this.getTrailer() != null && this.getTrailer().length() > 0 && Files.notExists(fileNameToWrite)) {
            System.out.println("Writing trailer: " + this.toString() + " into file " + fileNameToWrite);
            //FileUtils.copyURLToFile(new URL(getTrailer()), fileNameToWrite.toFile(), connectionTimeout, readTimeout);
        }
    }

    @Override
    public String toXML() {
        // TODO Auto-generated method stub
        return null;
    }

}
