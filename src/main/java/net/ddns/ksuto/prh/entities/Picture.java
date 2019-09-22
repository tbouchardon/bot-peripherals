package net.ddns.ksuto.prh.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

@Data
@EqualsAndHashCode(callSuper = false)
public class Picture extends LocatedObject {
    
    private Object        object;
    private BufferedImage referenceImage;
    private String        path;
    
    public Picture(String path) {
    
        this.path = path;
        try {
            URL url = this.getClass().getResource(path);
            this.referenceImage = ImageIO.read(url);
            this.setHeight(getReferenceImage().getHeight());
            this.setWidth(getReferenceImage().getWidth());
        }
        catch (IOException e) {
            // TODO : Catcher cette exception correctement !
            e.printStackTrace();
        }
    }
    
    @Override
    public String getHash() {
    
        return path.replaceAll(".*/", "");
    }
}