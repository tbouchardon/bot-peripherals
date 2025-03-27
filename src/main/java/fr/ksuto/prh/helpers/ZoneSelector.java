package fr.ksuto.prh.helpers;

import fr.ksuto.prh.peripherals.Screen;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

class ZoneSelector extends JFrame {
    
    private final Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
    private final int       SCREEN_HEIGHT = (int) dim_D.getHeight();
    private final int       SCREEN_WIDTH  = (int) dim_D.getWidth();
    Integer x1;
    Integer x2;
    Integer y1;
    Integer y2;
    
    Screen.Zone selectedZone = null;
    boolean     drawing      = false;
    
    public ZoneSelector() throws HeadlessException {
        
        setAlwaysOnTop(true);
        setUndecorated(true);
        setLocation(0, 0);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 0));
        JPanel jPanel = new JPanel() {
            
            @Override
            public void paintComponent(Graphics g) {
                
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g.create();
                
                g2d.setColor(new Color(0, 1.0f, 0, 0.05f));
                
                g2d.fillRect(0, 0, SCREEN_WIDTH , SCREEN_HEIGHT );
                
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(1f));
                
                if (x1 != null && x2 != null) {
                    g2d.drawRect(Math.min(x1, x2),
                                 Math.min(y1, y2),
                                 Math.abs(x2 - x1),
                                 Math.abs(y2 - y1));
                    g2d.drawString("(x1=" + Math.min(x1, x2) +", x2=" + Math.max(x1, x2) + ", y1=" + Math.min(y1, y2) + ", y2=" + Math.max(y1, y2) + ")",
                                   10,
                                   SCREEN_HEIGHT - 30);
                }
                
                g2d.dispose();
            }
        };
        jPanel.setOpaque(false);
        jPanel.setLocation(0, 0);
        jPanel.setPreferredSize(new Dimension(SCREEN_WIDTH , SCREEN_HEIGHT ));
        add(jPanel);
        
        pack();
        setVisible(true);
        
        MouseAdapter mouseAdapter = new MouseAdapter() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                
                super.mouseReleased(e);
                
                if (!drawing) {
                    x1 = e.getX();
                    y1 = e.getY();
                    x2 = null;
                    y2 = null;
                }
                else {
                    x2 = e.getX();
                    y2 = e.getY();
                }
                drawing = !drawing;
                
                jPanel.repaint();
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                
                super.mouseMoved(e);
                
                if (x1 == null || !drawing) {return;}
                
                x2 = e.getX();
                y2 = e.getY();
                
                jPanel.repaint();
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    selectedZone = new Screen.Zone(Math.min(x1, x2), Math.max(x1, x2), Math.min(y1, y2), Math.max(y1, y2));
                }
                
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    selectedZone = Screen.Zone.NONE;
                    close();
                }
            }
        });
        
        jPanel.repaint();
    }
    
    public void close() {
        
        dispose();
    }
    
    Screen.Zone getSelectedZone() {
        
        return selectedZone;
    }
    
    public static void main(String[] args) {
        
        System.out.println(selectZone());
    }
    
    public static Screen.Zone selectZone() {
        
        Screen.Zone zone = Screen.Zone.ALL;
        
        ZoneSelector zoneSelector = new ZoneSelector();
        while (zoneSelector != null && zoneSelector.getSelectedZone() == null) {
            try {
                Thread.sleep(250);
            }
            catch (InterruptedException ignore) {}
        }
        if (zoneSelector != null) {
            zone = zoneSelector.getSelectedZone();
            zoneSelector.close();
        }
        
        return zone;
    }
}