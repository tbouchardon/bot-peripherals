package fr.ksuto.prh.helpers;

import fr.ksuto.prh.peripherals.Screen;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

@EqualsAndHashCode(callSuper = true)
public class Painter extends JFrame {
    
    private final Dimension   dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    private final int         SCREEN_WIDTH  = (int) dim_D.getWidth();
    private final int         SCREEN_HEIGHT = (int) dim_D.getHeight();
    private       WhiteBoard  whiteBoard;
    private       Screen.Zone zone;
    private       String      title         = "";
    
    public Painter() {
        
        init(Screen.Zone.ALL);
    }
    
    public Painter(Screen.Zone zone, String title) {
        
        this.title = title;
        init(zone);
    }
    
    public static void main(String[] args) {
    
    }
    
    public void repaintWhiteBoard() {
        
        whiteBoard.repaint();
    }
    
    private void init(Screen.Zone zone) throws HeadlessException {
        
        this.zone = zone;
        
        setAlwaysOnTop(true);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        whiteBoard = new WhiteBoard();
        add(whiteBoard);
        pack();
        setVisible(true);
    }
    
    public Graphics getWhiteBoardGraphics() {
        
        return whiteBoard.getImage().getGraphics();
    }
    
    @Data
    @EqualsAndHashCode(callSuper = true)
    public class WhiteBoard extends JPanel {
        
        BufferedImage image = new BufferedImage(zone.getWidth(), zone.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
        public WhiteBoard() {
            
            setOpaque(false);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g.create();
            //            g2d.drawRect(0, 0, 250, getHeight() - 1);
            
            g2d.setColor(Color.MAGENTA);
            g2d.setStroke(new BasicStroke(4f));
            g2d.drawRect(zone.getXMin(), zone.getYMin(), zone.getWidth(), zone.getHeight());
            
            g2d.drawString(title,
                           zone.getXMin() + 5,
                           zone.getYMin() - 20 < 0 ? zone.getYMin() + 15 : zone.getYMin() - 10);
            
            g2d.drawImage(image, 0, 0, this);
            
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
