package fr.ksuto.prh.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.ksuto.prh.entities.PositionXY;
import fr.ksuto.prh.peripherals.MousePosition;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

public abstract class MouseCapture {
    
    private static final Logger logger = LoggerFactory.getLogger(MouseCapture.class);
    
    public MouseCapture() {
        
        Dimension dim_D         = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        int       SCREEN_WIDTH  = (int) dim_D.getWidth();
        int       SCREEN_HEIGHT = (int) dim_D.getHeight();
        
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        frame.setUndecorated(true);
        frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        frame.setLocation(0, 0);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(new Color(0, 0, 0, 0));
        JPanel jPanel = getjPanel(SCREEN_WIDTH, SCREEN_HEIGHT, frame);
        frame.add(jPanel);
        frame.pack();
        frame.setVisible(true);
    }
    
    public abstract void onClick(PositionXY position);
    
    private @NotNull JPanel getjPanel(int SCREEN_WIDTH, int SCREEN_HEIGHT, JFrame frame) {
        
        JPanel jPanel = new JPanel() {
            
            @Override
            public Dimension getPreferredSize() {
                
                return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
            }
        };
        jPanel.setBackground(new Color(0.5f, 0.25f, 0, 0.01f));
        jPanel.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent e) {
                
                super.mouseClicked(e);
                
                try {
                    MousePosition mousePosition = new MousePosition();
                    frame.setVisible(false);
                    onClick(mousePosition.getPosition());
                    frame.dispose();
                }
                catch (AWTException ex) {
                    logger.error("Error getting mouse position");
                }
            }
        });
        return jPanel;
    }
}
