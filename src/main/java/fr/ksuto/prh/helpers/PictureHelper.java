package fr.ksuto.prh.helpers;

import fr.ksuto.prh.entities.LocatedObject;
import fr.ksuto.prh.entities.Parameter;
import fr.ksuto.prh.peripherals.Peripheral;
import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PictureHelper {
    
    Robot robot = new Robot();
    
    public PictureHelper() throws AWTException {}
    
    public static Parameter findWorkingParameters(int numberOfMatches, AbstractSeeker seeker, int numberOfNoChangeLoops) {
    
        java.util.List<Parameter> parameters = new ArrayList<>();
        for (double e = 0.0; e <= 0.30; e += 0.05) {
            for (int p = 0; p < 66; p += 5) {
                parameters.add(new Parameter(p, e));
            }
        }
    
        List<LocatedObject> objects = seeker.getObjects();
        
        java.util.List<Parameter> top10BestParameters      = new ArrayList<>();
        Parameter                 closestParameters        = null;
        int                       closestParametersMatches = 0;
        int                       noChangeLoops            = 0;
        
        while ((!parameters.isEmpty() || !top10BestParameters.isEmpty()) && noChangeLoops < numberOfNoChangeLoops) {
            
            noChangeLoops++;
            
            while (!parameters.isEmpty() && top10BestParameters.size() < 10) {
                top10BestParameters.add(parameters.remove(0));
            }
            
            System.out.println("[TRACE] PRH : ------------------------------------------------------------------------------------------------------------------------");
            
            Iterator<Parameter> iterator = top10BestParameters.iterator();
            
            while (iterator.hasNext()) {
                
                Parameter param = iterator.next();
                seeker.setPrecision(param.getPrecision());
                seeker.setAllowedErrorRate(param.getErrorRate());
                long time = System.nanoTime();
                seeker.search();
                long   spent       = System.nanoTime() - time;
                String spentString = String.format("%1.2f", ((float) spent) / (1000 * 1000 * 1000));
                
                if (spent > 3L * 1000 * 1000 * 1000) {
                    System.out.println("[INFO] PRH : precision = " + param.getPrecision() + " && errorRate = " + param.getErrorRate() + " => Retiré, " + spentString + "s > 3s");
                    iterator.remove();
                    noChangeLoops = 0;
                }
                else if (seeker.getNumberOfResults() != numberOfMatches) {
                    System.out.println("[INFO] PRH : precision = " + param.getPrecision() + " && errorRate = " + param.getErrorRate() + " => Retiré, matche(s) " + seeker.getNumberOfResults() + " !=" +
                                       " " + numberOfMatches);
                    if (closestParameters == null || Math.abs(numberOfMatches - seeker.getNumberOfResults()) < Math.abs(numberOfMatches - closestParametersMatches)) {
                        closestParametersMatches = seeker.getNumberOfResults();
                        closestParameters = param;
                    }
                    iterator.remove();
                    noChangeLoops = 0;
                }
                else {
                    System.out.println("[INFO] PRH : precision = " + param.getPrecision() + " && errorRate = " + param.getErrorRate() + " => " + seeker.getNumberOfResults() + " matche(s)");
                    objects.get(0).getPositions().forEach(position -> {
                        System.out.println("[TRACE] PRH :     Area : " +
                                           position.getX() + ", " +
                                           position.getY() + ", " +
                                           (objects.get(0).getWidth() + position.getX()) + ", " +
                                           (objects.get(0).getHeight() + position.getY()));
                    });
                    System.out.println("[SUCCESS] PRH :     .setPrecision(" + param.getPrecision() + ").setAllowedErrorRate(" + param.getErrorRate() + ")");
                }
            }
        }
    
        System.out.println("[INFO] PRH : ");
        if (top10BestParameters.isEmpty()) {
            System.out.println("[WARN] PRH : No perfect parameters found, closest match :");
            System.out.println("[WARN] PRH : precision = " + closestParameters.getPrecision() + " && errorRate = " + closestParameters.getErrorRate() +
                               ", found " + closestParametersMatches + "/" + " " + numberOfMatches);
        }
        else {
            System.out.println("[SUCCESS] PRH : best match :");
            System.out.println("[WARN] PRH : precision = " + closestParameters.getPrecision() + " && errorRate = " + closestParameters.getErrorRate());
        }
        //        if (seeker.hasAnyResults()) {
        //            System.out.println("[TRACE] PRH : Positions : ");
        //            objects.get(0).getPositions().forEach(position -> {
        //                System.out.println(position.getX() + ":" + position.getY());
        //            });
        //        }
        return closestParameters;
    }
    
    public void pictureHelper(String url, int numberOfMatches, boolean learn) throws AWTException {
        
        pictureHelper(new String[]{url}, numberOfMatches, learn);
    }
    
    public void pictureHelper(String[] url, int numberOfMatches, boolean learn) throws AWTException {
        
        pictureHelper(url, numberOfMatches, new Screen.Zone(), learn);
    }
    
    public void pictureHelper(String[] urls, int numberOfMatches, Screen.Zone searchZone, boolean learn) throws AWTException {
        
        for (int countDown = 5; countDown >= 0; countDown--) {
            System.out.println("[TRACE] PRH : countDown = " + countDown);
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
        }
        else {
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