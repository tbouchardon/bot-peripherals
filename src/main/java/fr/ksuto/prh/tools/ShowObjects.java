package fr.ksuto.prh.tools;

import fr.ksuto.prh.entities.LocatedObject;
import fr.ksuto.prh.entities.Position;
import fr.ksuto.prh.peripherals.Peripheral;
import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class ShowObjects<T extends LocatedObject> extends JFrame {
    
    private final Dimension   dim_D          = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    private final int         SCREEN_HEIGHT  = (int) dim_D.getHeight();
    private final int         SCREEN_WIDTH   = (int) dim_D.getWidth();
    private       PaintPane   paintPane;
    private       Color       color          = Color.getHSBColor((float) Math.random(), 1f, 1f);
    private       List<T>     locatedObjects = new ArrayList<>();
    private       Screen.Zone zone;
    private       String      title          = "";
    private       String      splash         = "";
    
    public ShowObjects() {
        
        init(Screen.Zone.ALL);
    }
    
    public ShowObjects(Screen.Zone zone, String title) {
        
        this.title = title;
        init(zone);
    }
    
    public static void main(String[] args) throws AWTException {
    
        ShowObjects showObjects = new ShowObjects();
    
        showObjects.countDown(3);
    
        List<Position> positions = new ArrayList<>();
        positions.add(new Position(100, 500));
        Position position = new Position(200, 400);
        position.setHasMoved(true);
        positions.add(position);
        positions.add(new Position(300, 300));
        position = new Position(400, 200);
        position.setExplored(true);
        positions.add(position);
        position = new Position(500, 100);
        position.setExplored(true);
        position.setHasMoved(true);
        positions.add(position);
        
        List<LocatedObject> locatedObjectlist = new ArrayList<>();
        LocatedObject locatedObject = new LocatedObject() {
    
            @Override
            public String getHash() {
        
                return "null";
            }
        };
        locatedObject.setWidth(10);
        locatedObject.setHeight(15);
        locatedObject.setPositions(positions);
        locatedObjectlist.add(locatedObject);
        
        showObjects.setLocatedObjects(locatedObjectlist);
        
        while (true) {
            for (LocatedObject object : locatedObjectlist) {
                for (Position pos : object.getPositions()) {
                    pos.setY(pos.getY() + (int) (Math.random() * 3 - 1.5));
                    pos.setX(pos.getX() + (int) (Math.random() * 3 - 1.5));
                }
            }
            showObjects.setLocatedObjects(locatedObjectlist);
            Robot robot = new Robot();
            Peripheral.delay(25);
        }
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
    }
    
    public void setLocatedObjects(List<T> locatedObjects) {
        
        this.locatedObjects = locatedObjects;
        paintPane.repaint();
    }
    
    public class PaintPane extends JPanel {
        
        public PaintPane() {
            
            setOpaque(false);
        }
        
        @Override
        public void paintComponent(Graphics g) {
    
            super.paintComponent(g);
    
            Graphics2D g2d = (Graphics2D) g.create();
    
            if (splash != null && !splash.equals("")) {
                g2d.setColor(Color.LIGHT_GRAY);
                Font currentFont = g2d.getFont();
                Font newFont     = currentFont.deriveFont(currentFont.getSize() * 15F);
                g2d.setFont(newFont);
                g2d.drawString(splash, SCREEN_WIDTH / 2 - 50, SCREEN_HEIGHT / 2);
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
            
            int grow = 4;
            
            for (T locatedObject : locatedObjects) {
                
                int index = 0;
                
                for (Position position : locatedObject.getPositions()) {
                    
                    index++;
                    
                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawRect(position.getX() - grow, position.getY() - grow, locatedObject.getWidth() + grow * 2, locatedObject.getHeight() + grow * 2);
                    g2d.drawString(String.valueOf(index),
                                   position.getX() + locatedObject.getWidth() + grow * 2,
                                   position.getY() + locatedObject.getHeight() + grow * 2 - 5);
                    
                    if (position.isHasMoved()) {
                        g2d.setStroke(new BasicStroke(1));
                        g2d.setColor(Color.ORANGE);
                        g2d.drawRect(position.getX() - 2 - grow * 2, position.getY() - 2 - grow * 2, locatedObject.getWidth() + 3 + grow * 2, locatedObject.getHeight() + 3 + grow * 2);
                    }
                    if (position.isExplored()) {
                        g2d.setStroke(new BasicStroke(1));
                        g2d.setColor(Color.RED);
                        g2d.drawLine(position.getX() - 1 - grow, position.getY() - 1 - grow, position.getX() + locatedObject.getWidth() + grow * 2,
                                     position.getY() + locatedObject.getHeight() + grow * 2);
                        g2d.drawLine(position.getX() + locatedObject.getWidth() + grow * 2, position.getY() - 1 - grow, position.getX() - 1 - grow,
                                     position.getY() + locatedObject.getHeight() + grow * 2);
                    }
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
