package net.ddns.ksuto.prh;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.Callable;

public class RunnableRobot implements Callable<Integer> {
    
    final   Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    final   int       SCREEN_WIDTH  = (int) dim_D.getWidth();
    final   int       SCREEN_HEIGHT = (int) dim_D.getHeight();
    private long      startTime;
    private int       loop;
    
    public RunnableRobot(int loop, long startTime) {
        
        this.loop = loop;
        this.startTime = startTime;
    }
    
    @Override
    public Integer call() throws Exception {
        
        try {
            Robot         robot          = new Robot();
            BufferedImage capturedScreen = robot.createScreenCapture(new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT));
            
            int   p1     = 0;
            int[] pixels = ((DataBufferInt) capturedScreen.getRaster().getDataBuffer()).getData();
            for (int p : pixels) { p1 += p; }
            System.out.println(p1);
        }
        catch (AWTException e) {
            // TODO : Catcher cette exception correctement !
            e.printStackTrace();
        }
        
        return -1;
    }
}
