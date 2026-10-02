package fr.ksuto.prh.tools;

import fr.ksuto.prh.entities.LocatedObject;
import fr.ksuto.prh.entities.Position;
import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class ShowObjects<T extends LocatedObject> extends JFrame {
    
    private final Dimension dim_D          = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    private final int       SCREEN_HEIGHT  = (int) dim_D.getHeight();
    private final int       SCREEN_WIDTH   = (int) dim_D.getWidth();
    public        float     errorRateDelta = 0.0f;
    public        Integer   precision      = null;
    public        int       precisionDelta = 0;
    public  Point       mousePosition;
    Double errorRate = null;
    private boolean     shouldClose    = false;
    private PaintPane   paintPane;
    private Color       color          = Color.getHSBColor((float) Math.random(), 1f, 1f);
    private List<T>     objects        = new ArrayList<>();
    private Screen.Zone zone;
    private String      title          = "";
    private String      splash         = "";
    private boolean     showCount      = false;
    private boolean     controlKeyDown = false;
    
    public ShowObjects() {
        
        init(Screen.Zone.ALL);
    }
    
    public ShowObjects(Screen.Zone zone, String title) {
        
        this.title = title;
        init(zone);
    }
    
    public void clean() {
        
        paintPane.clear();
        dispose();
    }
    
    public void countDown(int startingFrom) {
        
        for (int i = startingFrom; i >= 0; i--) {
            splash = String.valueOf(i);
            paintPane.repaint();
            
            long first  = System.nanoTime();
            long second = System.nanoTime();
            
            while (second - first < (i > 0 ? 1000 : 250) * 1000000) {
                second = System.nanoTime();
            }
        }
        splash = "";
    }
    
    public boolean shouldClose() {
        
        return this.shouldClose;
    }
    
    private void init(Screen.Zone zone) {
        
        this.zone = zone;
        setAlwaysOnTop(true);
        setUndecorated(true);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));
        paintPane = new PaintPane();
        add(paintPane);
        pack();
        setVisible(true);
        addKeyListener(new KeyListener() {
            
            @Override
            public void keyTyped(KeyEvent e) {
            
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_CONTROL:
                        controlKeyDown = true;
                        break;
                }
//                System.out.println(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (controlKeyDown) {errorRateDelta += 0.05;}
                        else {precisionDelta += 5;}
                        break;
                    case KeyEvent.VK_DOWN:
                        if (controlKeyDown) {errorRateDelta -= 0.05;}
                        else {precisionDelta -= 5;}
                        break;
                    case KeyEvent.VK_CONTROL:
                        controlKeyDown = false;
                        break;
                    case KeyEvent.VK_ENTER:
                        mousePosition = MouseInfo.getPointerInfo().getLocation();
//                        System.out.println("Enter up");
//                        System.out.println(mousePosition);
                        break;
                    case KeyEvent.VK_ESCAPE:
                        shouldClose = true;
                        break;
                    default:
                        //                        System.out.println(e.getKeyCode());
                }
            }
        });
    }
    
    public void setErrorRate(Double errorRate) {
        
        this.errorRate = errorRate;
    }
    
    public void setLocatedObjects(List<T> locatedObjects) {
        
        this.objects = locatedObjects;
        paintPane.repaint();
    }
    
    public void setPrecision(Integer precision) {
        
        this.precision = precision;
    }
    
    public void setShowCount(boolean show) {
        
        showCount = show;
    }
    
    public class PaintPane extends JPanel {
        
        public PaintPane() {
            
            setOpaque(false);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            
            super.paintComponent(g);
            
            Graphics2D g2d         = (Graphics2D) g.create();
            Font       currentFont = g2d.getFont();
            
            if (splash != null && !splash.equals("")) {
                g2d.setColor(Color.LIGHT_GRAY);
                Font newFont = currentFont.deriveFont(currentFont.getSize() * 15F);
                g2d.setFont(newFont);
                g2d.drawString(splash, SCREEN_WIDTH / 2 - 50, SCREEN_HEIGHT / 2);
                g2d.setFont(currentFont);
                //                g2d.dispose();
                return;
            }
            //            g2d.drawRect(0, 0, 250, getHeight() - 1);
            
            g2d.setColor(Color.MAGENTA);
            g2d.setStroke(new BasicStroke(4f));
            g2d.drawRect(zone.getXMin(), zone.getYMin(), zone.getWidth(), zone.getHeight());
            
            g2d.drawString(title,
                           zone.getXMin() + 5,
                           zone.getYMin() - 20 < 0 ? zone.getYMin() + 10 : zone.getYMin() - 10);
            
            if (precision != null && errorRate != null) {
                g2d.drawString("(p:" + (Math.max((precision + precisionDelta), 0)) + ", er:" + new DecimalFormat("0.00").format(Math.max(errorRate + errorRateDelta, 0)) + ")",
                               zone.getXMin() + 5,
                               SCREEN_HEIGHT - zone.getYMin() + zone.getHeight() > 35 ? zone.getYMin() + zone.getHeight() + 15 : zone.getYMin() + zone.getHeight() - 20);
            }
            
            int grow = 4;
            
            for (int objectIndex = 0, locatedObjectsSize = objects.size(); objectIndex < locatedObjectsSize; objectIndex++) {
                
                T object = objects.get(objectIndex);
                
                for (int positionIndex = 0, positionsSize = object.getPositions().size(); positionIndex < positionsSize; positionIndex++) {
                    Position position = object.getPositions().get(positionIndex);
                    
                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawRect(position.getX() - grow, position.getY() - grow, object.getWidth() + grow * 2, object.getHeight() + grow * 2);
                    g2d.drawString(String.valueOf(positionIndex + 1),
                                   position.getX() + object.getWidth() + grow * 2,
                                   position.getY() + object.getHeight() + grow * 2 - 5);
                    
                    if (position.isHasMoved()) {
                        g2d.setStroke(new BasicStroke(1));
                        g2d.setColor(Color.ORANGE);
                        g2d.drawRect(position.getX() - 2 - grow * 2, position.getY() - 2 - grow * 2, object.getWidth() + 3 + grow * 2, object.getHeight() + 3 + grow * 2);
                    }
                    if (position.isExplored()) {
                        g2d.setStroke(new BasicStroke(1));
                        g2d.setColor(Color.RED);
                        g2d.drawLine(position.getX() - 1 - grow, position.getY() - 1 - grow, position.getX() + object.getWidth() + grow * 2,
                                     position.getY() + object.getHeight() + grow * 2);
                        g2d.drawLine(position.getX() + object.getWidth() + grow * 2, position.getY() - 1 - grow, position.getX() - 1 - grow,
                                     position.getY() + object.getHeight() + grow * 2);
                    }
                }
                
                if (showCount) {
                    Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.5F);
                    g2d.setFont(newFont);
                    g2d.setColor(Color.GREEN);
                    
                    int     xPos                       = zone.getXMin() + 10;
                    boolean atLeast250PixelsLeftOnLeft = zone.getXMin() > 250;
                    if (atLeast250PixelsLeftOnLeft) {xPos = zone.getXMin() - 250;}
                    boolean atLeast250PixelsLeftOnRight = SCREEN_WIDTH - (zone.getXMin() + zone.getWidth()) > 250;
                    if (atLeast250PixelsLeftOnRight) {xPos = zone.getXMin() + zone.getWidth() + 10;}
                    
                    int yPos = zone.getYMin() + 30;
                    if (!atLeast250PixelsLeftOnRight && !atLeast250PixelsLeftOnLeft) {
                        
                        boolean atLeast250PixelsLeftOnTop = zone.getYMin() > 250;
                        if (atLeast250PixelsLeftOnTop) {yPos = zone.getYMin() - 17 * objects.size() - 10;}
                        boolean atLeast250PixelsLeftOnBottom = SCREEN_HEIGHT - (zone.getYMin() + zone.getHeight()) > 250;
                        if (atLeast250PixelsLeftOnBottom) {yPos = zone.getYMin() + zone.getHeight() + 30;}
                    }
                    
                    g2d.drawString(object.getPositions().size() + " " + object.getHash(), xPos, yPos + 17 * objectIndex);
                    g2d.setFont(currentFont);
                }
            }
            
            g2d.dispose();
        }
        
        @Override
        public Dimension getPreferredSize() {
            
            return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
        }
        
        public void clear() {
            
            dispose();
        }
    }
}