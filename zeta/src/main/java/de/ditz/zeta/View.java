package de.ditz.zeta;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class View extends JPanel {

    ImageSource source;

    BufferedImage image;

    double x=0;
    double y=0;
    double scale = 0;
    
    public View(int width, int  height, ImageSource source) {
        //setSize(width, height);
        this.source = source;
        JLabel label = new JLabel();
        label.setSize(width, height);
        add(label);
    }

    public void update() {
        ImageSource source = this.source;
        if(source==null)
            return;

        int height = getHeight();
        int width = getWidth();
        BufferedImage image = this.image;

        if((image==null || image.getHeight()!=height || image.getWidth()!=width)) {
            image = new BufferedImage(width, height, TYPE_INT_RGB);
        }

        for (int iy = 0; iy < width; ++iy) {
            for (int ix = 0; ix < width; ++ix) {
                double x = (2 * ix - width) * scale / 2;
                double y = (2 * iy - height) * scale / 2;
                int rgb = source.rgb(x, y);
                image.setRGB(ix, iy, rgb);
            }
        }

        this.image = image;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintImage((Graphics2D) g);
    }

    protected void paintImage(Graphics2D g) {
        if(image!=null) {
            g.drawImage(image, 0, 0, this);
        }
    }
}
