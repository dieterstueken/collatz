package de.ditz.draw;

import de.ditz.collatz.Digitizer;

import javax.swing.*;
import java.awt.*;

import static de.ditz.draw.CollatzDiagram.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:58
 */
public class CollatzDiagram2 extends AbstractDiagram {

    static final Digitizer DIG6 = new Digitizer("012345") ;

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
                long p2 = (long) Math.rint(p2(x));
                System.out.format("%6d %4ss %.1f\n", p2, DIG6.digits(p2), p15(y));
            }
        };

        return pane.addLabels().addPainter(CollatzDiagram2::new);
    }

    public static double L232 = L2/L32;

    public CollatzDiagram2(Scale2D scales) {
        super(scales);
    }

    class Paint {
        final Graphics2D g;

        final double xl = scales.sx.lower();
        final double xh = scales.sx.upper();

        final double yl = scales.sy.lower();
        final double yh = scales.sy.upper();

        Paint(Graphics2D g) {
            this.g = g;
        }

        void paint() {

            baseline();

            double mh = p2(Math.min(xh, 20));

            // all odd > 1
            for (long m = 3; m < mh; m += 2) {

                double x = l2(m);
                if (x > xh)
                    break;

                int lm = lm(m);
                double y = (lm - x) * L232;

                if (y < yl)
                    continue;

                long m1 = 3 * (m + 1) / 2 - 1;
                double x1 = l2(m1);
                double y1 = (lm - x1) * L232;

                int ix = scales.sx.pix(x);
                int iy = scales.sy.pix(y);
                int kx = scales.sx.pix(x1);
                int ky = scales.sy.pix(y1);

                Color color = Color.BLUE;
                if (iy >= 0 && y < yh) {
                    //g.setColor(m % 3 == 0 ? Color.GREEN : Color.RED);
                    if(m % 3 != 0) {
                        g.setColor(Color.RED);
                        g.drawLine(Math.max(ix, 0), iy, scales.sx.len(), iy);
                    } else {
                        color = Color.GREEN;
                    }
                }

                if (kx >= 0) {
                    g.setColor(color);
                    g.drawLine(ix, iy, kx, ky);
                }
            }
        }

        void baseline() {
            int iy = scales.sy.pix(0.0);
            if(iy>=0 && iy<scales.sy.len()) {
                int ix = scales.sx.pix(0.0);
                g.setColor(Color.RED);
                g.drawLine(ix, iy, scales.sx.len(), iy);
            }
        }
    }

    public void paint2D(Graphics2D g) {
         new Paint(g).paint();
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
