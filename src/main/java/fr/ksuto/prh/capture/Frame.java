package fr.ksuto.prh.capture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;

/**
 * Image capturée avec un accès direct au tableau de pixels, bien plus rapide que {@link BufferedImage#getRGB(int, int)}.
 * <p>
 * Les coordonnées sont relatives à la frame ; {@link #x()} et {@link #y()} donnent sa position à l'écran.
 * Aucune vérification de bornes n'est faite : un x hors de la frame lit la ligne suivante.
 */
public final class Frame {

    private final BufferedImage source;
    private final BufferedImage image;
    private final int[]         pixels;
    private final int           width;
    private final int           height;
    private final int           x;
    private final int           y;
    private final int           alphaMask;

    private Frame(BufferedImage image, int x, int y) {

        this.source = image;
        this.image = isDirectlyReadable(image) ? image : toIntArgb(image);
        this.pixels = ((DataBufferInt) this.image.getRaster().getDataBuffer()).getData();
        this.width = this.image.getWidth();
        this.height = this.image.getHeight();
        this.x = x;
        this.y = y;
        // TYPE_INT_RGB ne stocke pas l'alpha : getRGB() le renvoie opaque
        this.alphaMask = this.image.getType() == BufferedImage.TYPE_INT_RGB ? 0xFF000000 : 0;
    }

    public static Frame of(BufferedImage image) {

        return new Frame(image, 0, 0);
    }

    public static Frame of(BufferedImage image, int x, int y) {

        return new Frame(image, x, y);
    }

    public static Frame of(BufferedImage image, Rectangle screenZone) {

        return new Frame(image, screenZone.x, screenZone.y);
    }

    private static boolean isDirectlyReadable(BufferedImage image) {

        int type = image.getType();
        if (type != BufferedImage.TYPE_INT_RGB && type != BufferedImage.TYPE_INT_ARGB) {return false;}
        if (!(image.getRaster().getDataBuffer() instanceof DataBufferInt)) {return false;}
        if (!(image.getSampleModel() instanceof SinglePixelPackedSampleModel sampleModel)) {return false;}

        // Une sous-image (getSubimage) partage le tableau de son parent : décalage ou largeur de ligne différents
        return sampleModel.getScanlineStride() == image.getWidth() &&
               image.getRaster().getSampleModelTranslateX() == 0 &&
               image.getRaster().getSampleModelTranslateY() == 0 &&
               image.getRaster().getDataBuffer().getOffset() == 0;
    }

    private static BufferedImage toIntArgb(BufferedImage source) {

        BufferedImage converted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D    graphics  = converted.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return converted;
    }

    /**
     * @return la couleur ARGB du pixel, identique à {@link BufferedImage#getRGB(int, int)}
     */
    public int rgb(int x, int y) {

        return pixels[y * width + x] | alphaMask;
    }

    public int red(int x, int y) {

        return Rgb.red(pixels[y * width + x]);
    }

    public int green(int x, int y) {

        return Rgb.green(pixels[y * width + x]);
    }

    public int blue(int x, int y) {

        return Rgb.blue(pixels[y * width + x]);
    }

    public boolean contains(int x, int y) {

        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * @return l'image lue par cette frame (convertie en TYPE_INT_ARGB si l'image d'origine n'était pas lisible directement)
     */
    public BufferedImage image() {

        return image;
    }

    /**
     * @return l'image d'origine, telle que passée à {@link #of(BufferedImage)}
     */
    public BufferedImage source() {

        return source;
    }

    public int width() {

        return width;
    }

    public int height() {

        return height;
    }

    public int x() {

        return x;
    }

    public int y() {

        return y;
    }
}
