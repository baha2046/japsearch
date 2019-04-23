package org.nagoya.model.dataitem;

import cyclops.control.Eval;
import cyclops.control.Future;
import cyclops.control.Try;
import io.vavr.control.Option;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;
import org.nagoya.FileDownloaderUtilities;
import org.nagoya.GUICommon;
import org.nagoya.system.Systems;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FxThumb extends MovieDataItem {
    public static final FxThumb BLANK_THUMB = new FxThumb();
    private static final long serialVersionUID = -5172283156078860446L;

    private Option<URL> thumbURL = Option.none();
    private Option<File> localPath = Option.none();
    private boolean loadAsync = true;
    private double widthLimit = 0;
    private double heightLimit = 0;

    private Eval<Image> fximage = Eval.later(this::loadLazy).mergeMap(i -> i);

    private String thumbLabel = "";


    private FxThumb() {
    }

    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    public static FxThumb of() {
        return new FxThumb();
    }

    public static FxThumb of(URL url) {
        FxThumb thumb = new FxThumb();
        thumb.thumbURL = Option.of(url);
        return thumb;
    }

    public static Option<FxThumb> of(String url) {
        return Try.withCatch(() -> new URL(url), MalformedURLException.class)
                .map(FxThumb::of)
                .peek(t -> {
                    if (url.startsWith("file:")) {
                        t.setLocalPath(new File(URI.create(url)));
                    }
                })
                .fold(Option::some, Option::none);
    }

    public static FxThumb of(Path url) {
        FxThumb thumb = FxThumb.of(url.toFile());
        thumb.setThumbURL(Try.withCatch(() -> url.toUri().toURL(), MalformedURLException.class).fold(Option::some, Option::none));
        return thumb;
    }

    public static FxThumb of(File url) {
        FxThumb thumb = new FxThumb();
        thumb.setLocalPath(url);
        thumb.setThumbURL(Try.withCatch(() -> url.toURI().toURL(), MalformedURLException.class).fold(Option::some, Option::none));
        return thumb;
    }

    /**
     * Utility function to of the last part of a URL formatted string (the filename) and return it. Usually used in conjunction with
     *
     * @param url
     * @return
     */
    public static String fileNameFromURL(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * Crops a JAV DVD jacket image so that only the cover is returned. This usually means the left half of the jacket image is cropped out.
     *
     * @param -        Image you wish to crop
     * @param filename - filename of the image. If you have a URL, you can of this from
     * @return A new BufferedImage object with the back part of the jacket cover cropped out
     */
    //        Rectangle2D viewportRect = new Rectangle2D(img.getWidth()/2, 0, maximumPosterSizeX, img.getHeight());
    public static Rectangle2D getCoverCrop(double width, double height, String filename) {
        //BufferedImage tempImage;
        int croppedWidth = (int) (width / 2.11);

        //Presets

        //SOD (SDMS, SDDE) - crop 3 pixels
        if (filename.contains("SDDE") || filename.contains("SDMS")) {
            croppedWidth = croppedWidth - 3;
        }
        //Natura High - crop 2 pixels
        if (filename.contains("NHDT")) {
            croppedWidth = croppedWidth - 2;
        }
        //HTY - crop 1 pixel
        if (filename.contains("HTV")) {
            croppedWidth = croppedWidth - 1;
        }
        //Prestige (EVO, DAY, ZER, EZD, DOM) crop 1 pixel
        if (filename.contains("EVO") || filename.contains("DAY") || filename.contains("ZER") || filename.contains("EZD") || filename.contains("DOM") && height == 522) {
            croppedWidth = croppedWidth - 1;
        }
        //DOM - overcrop a little
        if (filename.contains("DOM") && height == 488) {
            croppedWidth = croppedWidth + 13;
        }
        //DIM - crop 5 pixels
        if (filename.contains("DIM")) {
            croppedWidth = croppedWidth - 5;
        }
        //DNPD - the front is on the left and a different crop routine will be used below
        //CRZ - crop 5 pixels
        if (filename.contains("CRZ") && height == 541) {
            croppedWidth = croppedWidth - 5;
        }
        //FSET - crop 2 pixels
        if (filename.contains("FSET") && height == 675) {
            croppedWidth = croppedWidth - 2;
        }
        //Moodyz (MIRD dual discs - the original code says to center the overcropping but provides no example so I'm not dooing anything for now)
        //Opera (ORPD) - crop 1 pixel
        if (filename.contains("DIM")) {
            croppedWidth = croppedWidth - 1;
        }
        //Jade (P9) - crop 2 pixels
        if (filename.contains("P9")) {
            croppedWidth = croppedWidth - 2;
        }
        //Rocket (RCT) - Crop 2 Pixels
        if (filename.contains("RCT")) {
            croppedWidth = croppedWidth - 2;
        }
        //SIMG - crop 10 pixels
        if (filename.contains("SIMG") && height == 864) {
            croppedWidth = croppedWidth - 10;
        }
        //SIMG - crop 4 pixels
        if (filename.contains("SIMG") && height == 541) {
            croppedWidth = croppedWidth - 4;
        }
        //SVDVD - crop 2 pixels
        if (filename.contains("SVDVD") && height == 950) {
            croppedWidth = croppedWidth - 4;
        }
        //XV-65 - crop 6 pixels
        if (filename.contains("XV-65") && height == 750) {
            croppedWidth = croppedWidth - 6;
        }
        //800x538 - crop 2 pixels
        if (height == 538 && width == 800) {
            croppedWidth = croppedWidth - 2;
        }
        //800x537 - crop 1 pixel
        if (height == 537 && width == 800) {
            croppedWidth = croppedWidth - 1;
        }
        if (height == 513 && width == 800) {
            croppedWidth = croppedWidth - 14;
        }

	/*
		if (filename.contains("DNPD")) {
			tempImage = originalImage.getSubimage(0, 0, croppedWidth, height);
		} else
			tempImage = originalImage.getSubimage(width - croppedWidth, 0, croppedWidth, height);*/

        return new Rectangle2D(width - croppedWidth, 0, croppedWidth, height);
    }

    public static Try<Image, Exception> loadImageFromFile(@NotNull File path, double w, double h) {
        //GUICommon.debugMessage("FxThumb loadImageFromFile ");
        return cyclops.control.Try.withResources(() -> new FileInputStream(path),
                stream -> new javafx.scene.image.Image(stream, w, h, true, true),
                Exception.class)
                .onFail((e) -> GUICommon.debugMessage("loadImageFromFile >> File not found : " + path.toString()))
                ;
    }

    public static Try<Image, Exception> loadImageFromURL(@NotNull URL url, double w, double h) {
        //GUICommon.debugMessage("FxThumb loadImageFromURL >> " + url.toString());
        return cyclops.control.Try.withCatch(url::openConnection, Exception.class)
                .peek(conn -> conn.setRequestProperty("User-Agent", "Wget/1.13.4 (linux-gnu)"))
                .peek(conn -> conn.setRequestProperty("Referer", GUICommon.customReferrer(url, null)))
                .flatMap(conn -> cyclops.control.Try.withResources(conn::getInputStream,
                        stream -> new javafx.scene.image.Image(stream, w, h, true, true),
                        Exception.class))
                .onFail((e) -> GUICommon.debugMessage("FxThumb loadImageFromURL >> Cannot of image from : " + url.toString()))
                ;
    }

    public static boolean fileExistsAtUrl(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            URL url = new URL(URLName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            con.setRequestProperty("User-Agent", "Wget/1.13.4 (linux-gnu)");
            con.setRequestProperty("Referer", GUICommon.customReferrer(url, null));
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            System.out.println("------------ URL FILE NOT FOUND ------------");
            return false;
        }
    }

    public static void writeToFile(Image image, File file) {
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(image, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        try {
            ImageIO.write(bufImageRGB, "jpg", file);
        } catch (IOException e) {
            // TODO: handle exception here
            GUICommon.debugMessage("FxThumb writeToFile -- *** FAIL to write image file ***");
        }
        graphics.dispose();
    }

    private static ExecutorService currentThreadExecutorService() {
        ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
        return new ThreadPoolExecutor(0, 1, 0L, TimeUnit.SECONDS, new SynchronousQueue<>(), callerRunsPolicy) {
            @Override
            public void execute(Runnable command) {
                callerRunsPolicy.rejectedExecution(command, this);
            }
        };
    }

    public void setLocalPath(File path) {
        this.localPath = Option.of(path);
        this.loadAsync = false;
    }

    public boolean isLocal() {
        return this.localPath.isDefined();
    }

    public Image getImage() {
        this.loadAsync = false;
        return this.fximage.get();
    }

    public void setImage(@NotNull Image image) {
        this.fximage = Eval.eval(() -> image);
    }

    /**
     * load the image (use a file to replace the url)
     *
     * @param imageConsumer if null then run on current thread, otherwise on background thread
     */
    public void getImageLocal(@NotNull File path, Consumer<Image> imageConsumer) {
        this.setLocalPath(path);
        //GUICommon.debugMessage("getImageLocal");
        if (imageConsumer != null) {
            this.fximage.forEach(imageConsumer);
        }
    }

    public void getImage(Consumer<Image> imageConsumer) {
        //GUICommon.debugMessage("getImage");
        if (imageConsumer != null) {
            this.fximage.forEach(imageConsumer);
        }
    }

    public void fitInImageView(ImageView view, Option<Double> maxWidth, Option<Double> maxHeight) {
        this.getImage((img) -> {
            view.setImage(img);
            view.setPreserveRatio(true);

            maxWidth.peek(mw -> {
                if (img.getWidth() > mw) {
                    view.setFitWidth(mw);
                }
            });
            maxHeight.peek(mh -> {
                if (img.getHeight() > mh) {
                    view.setFitHeight(mh);
                }
            });
        });
    }

    private Future<Image> loadLazy() {
        //ExecutorService es = loadAsync ? GUICommon.getExecutorServices() : currentThreadExecutorService();
        // return loadAsync ?
        //         Future.async(GUICommon.getExecutorServices(),this::loadImage).flatMap(r->r) : loadImage();

        return this.localPath.isDefined() ?
                Future.fromTry(loadImageFromFile(this.localPath.get(), this.widthLimit, this.heightLimit)) :
                Future.of(() -> loadImageFromURL(this.thumbURL.get(), this.widthLimit, this.heightLimit), Systems.getExecutorServices()).mergeMap(i -> i);
    }

    /**
     * Write the current stored image to file, if not yet exist then load it first
     */
    public void writeToFile(File file) {
        this.getImage((img) -> writeToFile(img, file));
    }

    /**
     * Download a fresh image from the url and write it to file
     */
    public void writeUrlToFile(File file, boolean runInBackground) {
        Runnable runnable = () -> {
            try {
                FileDownloaderUtilities.writeURLToFile(this.getThumbURL(), file, this.getReferrerURL());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        if (runInBackground) {
            Systems.useExecutors(runnable);
        } else {
            runnable.run();
        }
    }

    public URL getThumbURL() {
        return this.thumbURL.getOrNull();
    }

    public boolean isEmpty() {
        return this.thumbURL == null;
    }

    public boolean notEmpty() {
        return this.thumbURL != null;
    }

    public void overwriteThumbURL(URL url) {
        this.thumbURL = Option.of(url);
    }

    /**
     * @return true if this thumb already exist in the cache and doesn't need to be downloaded again, false otherwise
     */
    public boolean isCached() {
        return this.fximage.isPresent();
    }

    @Override
    public String toXML() {
        return "<thumb>" + this.thumbURL.map(URL::getPath) + "</thumb>";
    }

    @Override
    public String toString() {
        return Objects.toString(this.thumbURL.getOrNull());
    }

    @Deprecated
    public void setPreviewURL(URL previewURL) {

    }

    @Deprecated
    public URL getReferrerURL() {
        return null;
    }

    @Deprecated
    public void setViewerURL(URL viewerURL) {

    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FxThumb)) return false;
        final FxThumb other = (FxThumb) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$thumbURL = this.getThumbURL();
        final Object other$thumbURL = other.getThumbURL();
        if (this$thumbURL == null ? other$thumbURL != null : !this$thumbURL.equals(other$thumbURL)) return false;
        final Object this$localPath = this.localPath;
        final Object other$localPath = other.localPath;
        if (this$localPath == null ? other$localPath != null : !this$localPath.equals(other$localPath)) return false;
        if (Double.compare(this.widthLimit, other.widthLimit) != 0) return false;
        if (Double.compare(this.heightLimit, other.heightLimit) != 0) return false;
        final Object this$thumbLabel = this.thumbLabel;
        final Object other$thumbLabel = other.thumbLabel;
        if (this$thumbLabel == null ? other$thumbLabel != null : !this$thumbLabel.equals(other$thumbLabel))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof FxThumb;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $thumbURL = this.getThumbURL();
        result = result * PRIME + ($thumbURL == null ? 43 : $thumbURL.hashCode());
        final Object $localPath = this.localPath;
        result = result * PRIME + ($localPath == null ? 43 : $localPath.hashCode());
        final long $widthLimit = Double.doubleToLongBits(this.widthLimit);
        result = result * PRIME + (int) ($widthLimit >>> 32 ^ $widthLimit);
        final long $heightLimit = Double.doubleToLongBits(this.heightLimit);
        result = result * PRIME + (int) ($heightLimit >>> 32 ^ $heightLimit);
        final Object $thumbLabel = this.thumbLabel;
        result = result * PRIME + ($thumbLabel == null ? 43 : $thumbLabel.hashCode());
        return result;
    }

    public Option<File> getLocalPath() {
        return this.localPath;
    }

    public String getThumbLabel() {
        return this.thumbLabel;
    }

    public void setThumbURL(Option<URL> thumbURL) {
        this.thumbURL = thumbURL;
    }

    public void setWidthLimit(double widthLimit) {
        this.widthLimit = widthLimit;
    }

    public void setHeightLimit(double heightLimit) {
        this.heightLimit = heightLimit;
    }

    public void setThumbLabel(String thumbLabel) {
        this.thumbLabel = thumbLabel;
    }
}
