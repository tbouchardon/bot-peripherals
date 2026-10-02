package fr.ksuto.prh.entities;

import fr.ksuto.prh.capture.Frame;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private transient Frame referenceFrame;
    
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
    
    /**
     * @return l'image de référence sous forme de {@link Frame} (accès rapide aux pixels), recalculée si l'image change
     */
    public Frame getReferenceFrame() {
        
        if (referenceFrame == null || referenceFrame.source() != referenceImage) {
            referenceFrame = Frame.of(referenceImage);
        }
        return referenceFrame;
    }
    
    @Override
    public String getHash() {
    
        return path.replaceAll(".*/", "").replace(".png", "");
    }
}