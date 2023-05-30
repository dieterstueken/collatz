package de.ditz.zeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import static javax.swing.SwingUtilities.invokeLater;

public class View extends JPanel {

    ImageSource source;

    double x0=0;
    double y0=0;
    double scale;

    int xoff = 0;
    int yoff = 0;

    ImageGenerator generator;
    
    public View(ImageSource source, double scale) {
        this.source = source;
        this.scale = scale;

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLater();
            }
        });

        MS ms = new MS();
        addMouseListener(ms);
        addMouseWheelListener(ms);
        addMouseMotionListener(ms);
    }

    class MS extends MouseAdapter {

        Point dragStart = null;

        public void mouseDragged(MouseEvent e) {
             if(dragStart==null)
                 dragStart = e.getPoint();
             else {
                 xoff = e.getX() - dragStart.x;
                 yoff = e.getY() - dragStart.y;
                 repaint();
             }
        }

        public void mouseReleased(MouseEvent e) {
            if(dragStart!=null) {
                xoff = e.getX() - dragStart.x;
                yoff = e.getY() - dragStart.y;
                x0 -= scale*xoff;
                y0 -= scale*yoff;
                xoff = 0;
                yoff = 0;
                dragStart = null;
                updateLater();
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (dragStart != null)
                return;

            double rotation = e.getPreciseWheelRotation();
            if (rotation == 0)
                return;

            double zoom = Math.pow(1.25, rotation);

            double dx = e.getX()-0.5*getWidth();
            double dy = e.getY()-0.5*getHeight();
            double ds = scale * (1-zoom);

            x0 += ds * dx;
            y0 += ds * dy ;
            scale *= zoom;

            updateLater();
        }
    }

    void stopGenerator() {
        ImageGenerator generator = this.generator;
        if(generator!=null) {
            this.generator = null;
            generator.interrupt();
        }
    }

    void updateLater() {
        stopGenerator();
        invokeLater(this::update);
    }

    void update() {
        int width = getWidth();
        int height = getHeight();

        if(source!=null && width!=0 && height!=0) {
            if(generator==null || generator.getWidth()!=width || generator.getHeight() != height) {
                stopGenerator();

                Image newImage = createImage(width, height);
                if (newImage instanceof BufferedImage) {
                    generator = new ImageGenerator(this, (BufferedImage) newImage);
                    generator.start();
                }
            }
        }
    }

    int rgb(int ix, int iy) {
        double x = scale * (2 * ix - getWidth()) / 2;
        double y = scale * (2 * iy - getHeight()) / 2;
        return source.rgb(x+x0, -y-y0);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintImage((Graphics2D) g);
    }

    protected void paintImage(Graphics2D g) {
        ImageGenerator generator = this.generator;
        if(generator!=null)
            g.drawImage(generator.image, xoff, yoff, null);
    }
}
