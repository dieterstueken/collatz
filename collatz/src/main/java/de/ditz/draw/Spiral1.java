package de.ditz.draw;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.02.26
 * Time: 16:31
 */
public class Spiral1 extends AbstractDiagram {

    public static void main(String ... args) {
        SwingUtilities.invokeLater(Spiral1::openFrame);
    }

    int turns = 16;
    static final double STEP = 0.0125;
    static final double L15 = Math.log(1.5);

    static void openFrame() {
        Pane2D pane = openPane();
        Pane2D.openFrame(pane);
        pane.center();
    }

    static Pane2D openPane() {

        Pane2D pane = new Pane2D(32) {
            @Override
            protected void mouseMoved(double x, double y) {
                super.mouseMoved(x, y);
            }
        };

        return pane.addLabels().addPainter(Spiral1::new);
    }

    Spiral1(Scale2D scales) {
        super(scales);
    }

    public void paint2D(Graphics2D g) {
        new Paint(g).paint();
    }

    class Paint {
        final Graphics2D g;

        Paint(Graphics2D g) {
            this.g = g;
        }

        void paint() {
            spiral(Color.GRAY);
            dots(Color.BLUE);
        }

        void spiral(Color color) {
            g.setColor(color);
            double x0 = 0;
            double y0 = 0;
            for(double t=STEP; t<turns; t+=STEP) {
                double phi = t*2*Math.PI;
                double x1  = t*Math.sin(phi);
                double y1  = t*Math.cos(phi);
                drawLine(x0, y0, x1, y1);
                x0 = x1;
                y0 = y1;
            }
        }

        void dots(Color color) {
            g.setColor(color);
            for(int i=1; i<657; ++i) {
                dot(i);
            }
        }

        void dot(double p) {
            double t = Math.log(p)/L15;
            double phi = t*2*Math.PI;
            double x1  = t*Math.sin(phi);
            double y1  = t*Math.cos(phi);

            drawOval(x1, y1, 0.25);
        }

        private void drawOval(double x, double y, double s) {
            int sx = (int) Math.ceil(2*scales.sx.dpu*s);
            int sy = (int) Math.ceil(-2*scales.sy.dpu*s);
            int ix = scales.sx.pix(x-s);
            int iy = scales.sy.pix(y+s);
            g.fillOval(ix, iy, sx, sy);
        }

        private void drawLine(double x0, double y0, double x1, double y1) {
            int ix0 = scales.sx.pix(x0);
            int iy0 = scales.sy.pix(y0);
            int ix1 = scales.sx.pix(x1);
            int iy1 = scales.sy.pix(y1);
            g.drawLine(ix0, iy0, ix1, iy1);
        }
    }
}
