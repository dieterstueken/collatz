package de.ditz.draw;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:58
 */
public class CollatzDiagram2 extends AbstractDiagram {

    public static void main(String ... args) {
        SwingUtilities.invokeLater(CollatzDiagram2::openFrame);
    }

    static void openFrame() {
        Pane2D.openFrame(openPane());
    }

    static Pane2D openPane() {

        Pane2D pane = new Pane2D(32) {

            @Override
            protected void mouseMoved(double x, double y) {
                //super.mouseMoved(x, y);
                System.out.format("%.0f %.1f\n", p2(x), p2(y));
            }
        };

        return pane.addLabels().addPainter(CollatzDiagram2::new);
    }

    public CollatzDiagram2(Scale2D scales) {
        super(scales);
    }

    static final double L2 = Math.log(2);

    static double l2(double value) {
        return Math.log(value) / L2;
    }

    static double p2(double value) {
        return Math.pow(2, value);
    }

    public void paint2D(Graphics2D g) {

        baseline(g);

        final double xl = scales.sx.lower();
        final double xh = scales.sx.upper();

        final double yl = scales.sy.lower();
        final double yh = scales.sy.upper();

        double nh = p2(Math.min(xh, 63));

        // all even > 0
        for(long m=2; m<nh; m+=2) {

            double x = l2(m - 1);
            if(x>xh)
                break;

            double y = lm(m - 1) - x;

            if (y < yl)
                continue;

            long m1 = 3 * m / 2;
            double x1 = l2(m1 - 1);
            double y1 = y + x - x1;

            if (y1 > yh)
                continue;

            int ix = scales.sx.pix(x);
            int iy = scales.sy.pix(y);
            int kx = scales.sx.pix(x1);
            int ky = scales.sy.pix(y1);

            if(iy>=0) {
                g.setColor(m % 6 == 4 ? Color.GREEN : Color.RED);
                g.drawLine(Math.max(ix, 0), iy, scales.sx.len(), iy);
            }

            if(ix>=0) {
                g.setColor(Color.BLUE);
                g.drawLine(ix, iy, kx, ky);
            }
        }
    }

    void baseline(Graphics2D g) {
        int iy = scales.sy.pix(0.0);
        if(iy>=0 && iy<scales.sy.len()) {
            int ix = scales.sx.pix(0.0);
            g.setColor(Color.RED);
            g.drawLine(ix, iy, scales.sx.len(), iy);
        }
    }

    int lm(long m) {
        int l = 0;

        while(m>1) {
            ++m;
            while(m%2==0) {
                m /= 2; m*= 3;
            }
            --m;
            int l2 = Long.numberOfTrailingZeros(m);
            m >>= l2;
            l += l2;
        }

        return l;
    }
}
