package de.ditz.zeta;

import java.awt.image.BufferedImage;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.05.23
 * Time: 23:27
 */
class ImageGenerator extends Thread {

    final View view;

    final BufferedImage image;

    ImageGenerator(View view, BufferedImage image) {
        super("ImageGenerator");
        this.view = view;
        this.image = image;
    }

    public void run() {

        int width = getWidth();
        int height = getHeight();

        int yl = 0;
        for (int iy = 0; iy < height; ++iy) {
            for (int ix = 0; ix < width; ++ix) {

                if (Thread.currentThread().isInterrupted())
                    return;

                int rgb = view.rgb(ix, iy);
                image.setRGB(ix, iy, rgb);
            }

            if ((iy % 16) == 15)
                yl = repaint(yl, iy + 1);
        }

        repaint(yl, height);
    }

    int repaint(int yl, int iy) {
        if (iy > yl)
            invokeLater(() -> repaintView(yl, iy - yl));
        return yl + 16;
    }
    
    void repaintView(int y, int height) {
        if(view.generator!=this)
            this.interrupt();
        else
            view.repaint(0, y, getWidth(), height);
    }

    public int getWidth() {
        return image.getWidth();
    }
    public int getHeight() {
        return image.getHeight();
    }
}
