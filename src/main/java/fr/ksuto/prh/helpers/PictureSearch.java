package fr.ksuto.prh.helpers;

import fr.ksuto.prh.entities.AbstractPictureEnum;
import fr.ksuto.prh.entities.Parameter;
import fr.ksuto.prh.entities.Picture;
import fr.ksuto.prh.entities.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Locale;

public class PictureSearch extends AbstractSeeker<PictureSearch, Picture> {

    public PictureSearch() throws AWTException {

        super();
    }

    public static PictureSearch getDefault(AbstractPictureEnum pictureEnum) throws AWTException {

        return getDefault(pictureEnum, 20, 0.05);
    }

    public static PictureSearch getDefault(AbstractPictureEnum pictureEnum, int precision, double errorRate) throws AWTException {

        PictureSearch pictureSearch = new PictureSearch();

        if (pictureEnum != null) {
            pictureSearch.addPictureWithUrl(pictureEnum.getUrl());
        }

        pictureSearch
                .setPrecision(precision)
                .setAllowedErrorRate(errorRate);

        return pictureSearch;
    }

    public PictureSearch findWorkingParameters(int numberOfMatches, int numberOfNoChangeLoops) {

        setShowTargets(true);
        setTracking(true);
        startDebug();

        setCountDown(3);

        Parameter workingParameters = PictureHelper.findWorkingParameters(numberOfMatches, this, numberOfNoChangeLoops);

        setShowTargets(false);
        setTracking(false);
        stopDebug();

        clearResults();

        setPrecision(workingParameters.getPrecision());
        setAllowedErrorRate(workingParameters.getErrorRate());

        return this;
    }

    public PictureSearch findWorkingParameters(int numberOfMatches) {

        return findWorkingParameters(numberOfMatches, 1);
    }

    @Override
    boolean isObjectFound(BufferedImage capturedScreen, Position currentPosition, Picture object) {

        return isPictureFound(capturedScreen, object, currentPosition);
    }

    @Override
    boolean searchObject(BufferedImage capturedScreen, Position currentPosition, Picture picture) {

        if (isPictureFound(capturedScreen, picture, currentPosition)) {

            picture.getPositions().add(new Position(currentPosition.getX() + searchZone.getXMin(), currentPosition.getY() + searchZone.getYMin()));
            picture.setPresent(true);
            return true;
        }
        return false;
    }

    public PictureSearch addPicture(AbstractPictureEnum pictureEnum, Object o) {

        Picture picture = new Picture(pictureEnum.getUrl());
        picture.setObject(o);

        objects.add(picture);

        return this;
    }

    public PictureSearch addPicture(AbstractPictureEnum pictureEnum) {

        this.objects.add(new Picture(pictureEnum.getUrl()));

        return this;
    }

    public PictureSearch addPicture(Picture picture) {

        this.objects.add(picture);

        return this;
    }

    public PictureSearch addPictureWithUrl(String url) {

        this.objects.add(new Picture(url));

        return this;
    }

    public PictureSearch addPictureWithUrl(String url, Object o) {

        Picture picture = new Picture(url);
        picture.setObject(o);

        objects.add(picture);

        return this;
    }

    public PictureSearch addPicturesWithUrls(String[] urls) {

        for (String url : urls) {
            addPictureWithUrl(url);
        }

        return this;
    }

    public void export() {

        System.out.println("PictureSearch objectSearch = (PictureSearch) PictureSearch.getDefault(" + objects.get(0).getHash().replace(".png", "").toUpperCase(Locale.ROOT) + ")");
        if (objects.size() > 1) {
            for (int i = 1, objectsSize = objects.size(); i < objectsSize; i++) {
                Picture object = objects.get(i);
                System.out.println("        .addPicture(" + object.getHash().replace(".png", "").toUpperCase(Locale.ROOT) + ")");
            }
        }
        System.out.println("        .setSearchZone(new Screen.Zone(" + searchZone.getXMin() + ", " + searchZone.getXMax() + ", " + searchZone.getYMin() + ", " + searchZone.getYMax() + "))");
        if (getAllowedErrorRate() != 0.05d)
            System.out.println("        .setAllowedErrorRate(" + new DecimalFormat("0.00").format(getAllowedErrorRate()).replace(",", ".") + ")");
        if (getPrecision() != 20) System.out.println("        .setPrecision(" + getPrecision() + ")");
        System.out.println("        .await().clickFirst();");

    }

    private boolean isPictureFound(BufferedImage capturedScreen, Picture picture, Position currentPosition) {

        if (picture.getReferenceImage().getHeight() + currentPosition.getY() >= capturedScreen.getHeight()) {
            return false;
        }
        if (picture.getReferenceImage().getWidth() + currentPosition.getX() >= capturedScreen.getWidth()) {
            return false;
        }

        int tempCapturedRGB;
        int refRGB = picture.getReferenceImage().getRGB(0, 0);
        int capturedRGB = capturedScreen.getRGB(currentPosition.getX(), currentPosition.getY());
        double area = (double) picture.getReferenceImage().getHeight() * (double) picture.getReferenceImage().getWidth();
        double errorNumber = 0;

        boolean found = false;

        if (isMatch(capturedRGB, refRGB)) {

            found = true;

            yxLoop:
            for (int yRef = 0; yRef < picture.getReferenceImage().getHeight(); yRef++) {

                for (int xRef = 0; xRef < picture.getReferenceImage().getWidth(); xRef++) {

                    tempCapturedRGB = capturedScreen.getRGB(xRef + currentPosition.getX(), yRef + currentPosition.getY());
                    refRGB = picture.getReferenceImage().getRGB(xRef, yRef);

                    boolean match = isMatch(tempCapturedRGB, refRGB);
                    if (!match) {
                        errorNumber++;
                    }

                    double errorRate = errorNumber / area;

                    if (errorRate > getAllowedErrorRate()) {
                        found = false;
                        break yxLoop;
                    }
                }
            }
        }

        return found;
    }
}

