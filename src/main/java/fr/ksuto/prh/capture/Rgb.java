package fr.ksuto.prh.capture;

/**
 * Manipulation des couleurs au format ARGB renvoyé par {@link java.awt.image.BufferedImage#getRGB(int, int)} et {@link Frame#rgb(int, int)}.
 */
public final class Rgb {

    public static final int ARGB_BLACK = 0xFF000000;
    public static final int ARGB_BLUE  = 0xFF0000FF;
    public static final int ARGB_GREEN = 0xFF00FF00;
    public static final int ARGB_RED   = 0xFFFF0000;
    public static final int ARGB_WHITE = 0xFFFFFFFF;

    private Rgb() {}

    public static int red(int rgb) {

        return (rgb >> 16) & 0xFF;
    }

    public static int green(int rgb) {

        return (rgb >> 8) & 0xFF;
    }

    public static int blue(int rgb) {

        return rgb & 0xFF;
    }

    public static int of(int red, int green, int blue) {

        return 0xFF000000 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    /**
     * @return vrai si chaque composante de {@code rgb} est strictement à moins de {@code precision} de celle de (red, green, blue)
     */
    public static boolean isClose(int rgb, int red, int green, int blue, int precision) {

        return Math.abs(red(rgb) - red) < precision &&
               Math.abs(green(rgb) - green) < precision &&
               Math.abs(blue(rgb) - blue) < precision;
    }

    /**
     * @return vrai si chaque composante de {@code rgb} est strictement à moins de {@code precision} de celle de {@code reference}
     */
    public static boolean isClose(int rgb, int reference, int precision) {

        return isClose(rgb, red(reference), green(reference), blue(reference), precision);
    }
}
