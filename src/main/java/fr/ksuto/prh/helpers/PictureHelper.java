package fr.ksuto.prh.helpers;

import fr.ksuto.logger.ConsoleLogger;
import fr.ksuto.prh.entities.AbstractPictureEnum;
import fr.ksuto.prh.entities.LocatedObject;
import fr.ksuto.prh.entities.Parameter;
import fr.ksuto.prh.peripherals.Peripheral;
import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static fr.ksuto.prh.helpers.ZoneSelector.selectZone;

public class PictureHelper {

    Robot robot = new Robot();

    public PictureHelper() throws AWTException {
    }

    public static Parameter findWorkingParameters(int numberOfMatches, AbstractSeeker seeker, int numberOfNoChangeLoops) {

        ConsoleLogger logger = new ConsoleLogger();

        java.util.List<Parameter> parameters = new ArrayList<>();
        for (double e = 0.0; e <= 0.30; e += 0.05) {
            for (int p = 0; p < 66; p += 5) {
                parameters.add(new Parameter(p, e));
            }
        }

        List<LocatedObject> objects = seeker.getObjects();

        java.util.List<Parameter> top10BestParameters = new ArrayList<>();
        Parameter closestParameters = null;
        int closestParametersMatches = 0;
        int noChangeLoops = 0;

        while ((!parameters.isEmpty() || !top10BestParameters.isEmpty()) && noChangeLoops < numberOfNoChangeLoops) {

            noChangeLoops++;

            while (!parameters.isEmpty() && top10BestParameters.size() < 10) {
                top10BestParameters.add(parameters.remove(0));
            }

            logger.sysOutTrace("------------------------------------------------------------------------------------------------------------------------");

            Iterator<Parameter> iterator = top10BestParameters.iterator();

            while (iterator.hasNext()) {

                Parameter param = iterator.next();
                seeker.setPrecision(param.getPrecision());
                seeker.setAllowedErrorRate(param.getErrorRate());
                long time = System.nanoTime();
                seeker.search();
                long spent = System.nanoTime() - time;
                String spentString = String.format("%1.2f", ((float) spent) / (1000 * 1000 * 1000));

                if (spent > 3L * 1000 * 1000 * 1000) {

                    logger.sysOutInfo("precision = " + param.getPrecision() + " && errorRate = " + param.getErrorRate() + " => Retiré, " + spentString + "s > 3s");
                    iterator.remove();
                    noChangeLoops = 0;
                } else if (seeker.getNumberOfResults() != numberOfMatches) {
                    logger.sysOutInfo("precision = " + param.getPrecision() + " && errorRate = " + param.getErrorRate() + " => Retiré, matche(s) " + seeker.getNumberOfResults() + " !=" +
                            " " + numberOfMatches);
                    iterator.remove();
                    noChangeLoops = 0;
                } else {
                    logger.sysOutInfo("precision = " + param.getPrecision() + " && errorRate = " + param.getErrorRate() + " => " + seeker.getNumberOfResults() + " matche(s)");
                    objects.get(0).getPositions().forEach(position -> {
                        logger.sysOutTrace("    Area : " +
                                position.getX() + ", " +
                                position.getY() + ", " +
                                (objects.get(0).getWidth() + position.getX()) + ", " +
                                (objects.get(0).getHeight() + position.getY()));
                    });
                    logger.sysOutInfo("    .setPrecision(" + param.getPrecision() + ").setAllowedErrorRate(" + param.getErrorRate() + ")");
                }
                if (seeker.getNumberOfResults() != 0 && (closestParameters == null || Math.abs(numberOfMatches - seeker.getNumberOfResults()) < Math.abs(numberOfMatches - closestParametersMatches))) {
                    closestParametersMatches = seeker.getNumberOfResults();
                    closestParameters = param;
                }
            }
        }

        if (!top10BestParameters.isEmpty()) {
            logger.sysOutSuccess("Top 10 best parameters :");
            top10BestParameters.forEach(parameter -> {
                logger.sysOutSuccess("    .setPrecision(" + parameter.getPrecision() + ").setAllowedErrorRate(" + parameter.getErrorRate() + ")");
            });
        }
        if (closestParameters == null) {
            logger.sysOutError("No working parameters found.");
        } else if (top10BestParameters.isEmpty()) {
            logger.sysOutWarning("No perfect parameters found, closest match :");
            logger.sysOutWarning("precision = " + closestParameters.getPrecision() + " && errorRate = " + closestParameters.getErrorRate() +
                    ", found " + closestParametersMatches + "/" + " " + numberOfMatches);
        } else {
            logger.sysOutSuccess("Best match :");
            logger.sysOutWarning("precision = " + closestParameters.getPrecision() + " && errorRate = " + closestParameters.getErrorRate());
        }
        //        if (seeker.hasAnyResults()) {
        //            logger.sysOutTrace("Positions : ");
        //            objects.get(0).getPositions().forEach(position -> {
        //                System.out.println(position.getX() + ":" + position.getY());
        //            });
        //        }
        return closestParameters;
    }

    public static void freeSearch(AbstractPictureEnum pictureEnum) throws AWTException {

        freeSearch(new AbstractPictureEnum[]{pictureEnum});
    }

    public static void freeSearch(AbstractPictureEnum[] pictureEnums) throws AWTException {

        freeSearch(pictureEnums, null, 0.05, 20);
    }

    public static void freeSearch(AbstractPictureEnum[] pictureEnums, Screen.Zone searchZone, double errorRate, int precision) throws AWTException {

        ConsoleLogger logger = new ConsoleLogger();

        if (searchZone == null) {
            searchZone = selectZone();
        }

        if (searchZone.getHeight() == 0 || searchZone.getWidth() == 0) return;

        PictureSearch pictureSearch = (PictureSearch) new PictureSearch()
                .addPicturesWithUrls(Arrays.stream(pictureEnums).map(AbstractPictureEnum::getUrl).toArray(String[]::new))
                .setSearchZone(searchZone)
                .setShowTargets(true)
                .setTracking(true)
                .setAllowedErrorRate(errorRate)
                .setPrecision(precision)
                .startDebug();

        while (!pictureSearch.shouldClose()) {
            pictureSearch.search();
        }

        int numberOfMatches = pictureSearch.getAllPositions().size();
        if (numberOfMatches >= 1) logger.sysOutSuccess("Found " + numberOfMatches + " matches !");
        if (numberOfMatches == 0) logger.sysOutWarning("No matches found =(");
        logger.sysOutInfo("Picture search builder :");
        pictureSearch.export();
        pictureSearch.stopDebug();

    }

    public void pictureHelper(String url, int numberOfMatches, boolean learn) throws AWTException {

        pictureHelper(new String[]{url}, numberOfMatches, learn);
    }

    public void pictureHelper(String[] url, int numberOfMatches, boolean learn) throws AWTException {

        pictureHelper(url, numberOfMatches, new Screen.Zone(), learn);
    }

    public void pictureHelper(String[] urls, int numberOfMatches, Screen.Zone searchZone, boolean learn) throws AWTException {

        ConsoleLogger logger = new ConsoleLogger();

        for (int countDown = 5; countDown >= 0; countDown--) {
            logger.sysOutInfo("countDown = " + countDown);
            Peripheral.delay(1000);
        }

        if (learn) {
            PictureSearch search = (PictureSearch) new PictureSearch()
                    .addPicturesWithUrls(urls)
                    .setSearchZone(searchZone)
                    .setShowTargets(true)
                    .setTracking(true)
                    .startDebug()
                    .learn(numberOfMatches)
                    .optimize()
                    .search();

            search.clean();
        } else {
            PictureSearch pictureSearch = (PictureSearch) new PictureSearch()
                    .addPicturesWithUrls(urls)
                    .setSearchZone(searchZone)
                    .setShowTargets(true)
                    .setTracking(true)
                    .startDebug();

            findWorkingParameters(numberOfMatches, pictureSearch, Integer.MAX_VALUE);

            pictureSearch.clean();
        }
    }
}