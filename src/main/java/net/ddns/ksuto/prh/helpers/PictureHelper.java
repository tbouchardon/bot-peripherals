package net.ddns.ksuto.prh.helpers;

import net.ddns.ksuto.prh.entities.LocatedObject;
import net.ddns.ksuto.prh.entities.Parameter;
import net.ddns.ksuto.prh.peripherals.Screen;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PictureHelper {
    
    Robot robot = new Robot();
    
    public PictureHelper() throws AWTException {}
    
    public void pictureHelper(String url, int numberOfMatches, boolean learn) throws AWTException {
    
        pictureHelper(url, numberOfMatches, new Screen.Zone(), learn);
    }
    
    public void pictureHelper(String url, int numberOfMatches, Screen.Zone searchZone, boolean learn) throws AWTException {
        
        for (int countDown = 5; countDown >= 0; countDown--) {
            System.out.println("countDown = " + countDown);
            robot.delay(1000);
        }
        
        if (learn) {
            PictureSearch search = (PictureSearch) new PictureSearch()
                                                           .addPictureWithUrl(url)
                                                           .setSearchZone(searchZone)
                                                           .setShowTargets(true)
                                                           .setTracking(true)
                                                           .debug()
                                                           .learn(numberOfMatches)
                                                           .optimize()
                                                           .search();
            
            search.clean();
        }
        else {
            PictureSearch pictureSearch = (PictureSearch) new PictureSearch()
                                                                  .addPictureWithUrl(url)
                                                                  .setSearchZone(searchZone)
                                                                  .setShowTargets(true)
                                                                  .setTracking(true)
                                                                  .debug();
            
            findWorkingParameters(numberOfMatches, pictureSearch);
            
            pictureSearch.clean();
        }
    }
    
    private void findWorkingParameters(int numberOfMatches, AbstractSeeker seeker) {
        
        java.util.List<Parameter> parameters = new ArrayList<>();
        for (int p = 0; p < 66; p += 5) {
            for (double e = 0.0; e <= 0.30; e += 0.05) {
                parameters.add(new Parameter(p, e));
            }
        }
        
        List<LocatedObject> objects = seeker.getObjects();
        
        while (!parameters.isEmpty()) {
            
            System.out.println("------------------------------------------------------------------------------------------------------------------------");
            
            Iterator<Parameter> iterator = parameters.iterator();
            
            while (iterator.hasNext()) {
                
                Parameter param = iterator.next();
                seeker.setPrecision(param.getPrecision());
                seeker.setAllowedErrorRate(param.getErrorRate());
                seeker.search();
    
                if (objects.get(0).getPositions().size() != numberOfMatches) { iterator.remove(); }
                else {
                    System.out.println("precision = " + param.getPrecision() + " && errorRate = " + param.getErrorRate() + " => " + objects.get(0).getPositions().size() + " matche(s)");
                    objects.get(0).getPositions().forEach(position -> {
                        System.out.println("    Area : " +
                                           position.getX() + ", " +
                                           position.getY() + ", " +
                                           (objects.get(0).getWidth() + position.getX()) + ", " +
                                           (objects.get(0).getHeight() + position.getY()));
                    });
                    System.out.println("    .setPrecision(" + param.getPrecision() + ").setAllowedErrorRate(" + param.getErrorRate() + ")");
                }
            }
        }
        
        if (seeker.hasAnyResults()) {
            System.out.println("Positions : ");
            objects.get(0).getPositions().forEach(position -> {
                System.out.println(position.getX() + ":" + position.getY());
            });
        }
    }
}