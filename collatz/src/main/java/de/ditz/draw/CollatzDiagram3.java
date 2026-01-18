package de.ditz.draw;

import javax.swing.*;
import java.awt.*;

import static de.ditz.draw.CollatzDiagram.l2;
import static de.ditz.draw.CollatzDiagram.p2;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:58
 */
public class CollatzDiagram3 extends AbstractDiagram {

    public static void main(String ... args) {
        SwingUtilities.invokeLater(CollatzDiagram3::openFrame);
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

        return pane.addLabels().addPainter(CollatzDiagram3::new);
    }

    public CollatzDiagram3(Scale2D scales) {
        super(scales);
    }

    class Paint {
        final Graphics2D g;
        final double xl;
        final double xh;
        final double yl;
        final double yh;

        Paint(Graphics2D g) {
            this.g = g;
            this.xl = scales.sx.lower();
            this.xh = scales.sx.upper();
            this.yh = scales.sy.upper();
            this.yl = scales.sy.lower();
        }

        void paint() {
            if(xl<=0)
                hline(0, 0);
            
            paint(1, 0, 0);
        }

        /**
         * Paint the graph at given position.
         * @param mx current value of current floor (even).
         * @param x ln2(x).
         * @param my # of floor
         */
        void paint(long mx, double x, int my) {

            // exceeded above
            if(my-1>yh)
                return;

            final long m3 = mx % 3;

            if(my<=yh) {
                assert m3!=0;
                hline(x, my);
            }

            if(m3==1) {
                if(mx==1) {
                    // skip loop 2->1
                    mx = 8;
                    x = 3;
                } else {
                    mx *= 2; // odd jump
                    x += 1;
                }
            }

            while(x<xh) {
                long nx = (mx + 1) / 3 * 2 - 1;
                double x1 = l2(nx);

                if(nx%3==0) {
                    vline(x1, x, my, Color.GREEN);
                } else {
                    vline(x1, x, my, Color.BLUE);
                    paint(nx, x1, my + 1);
                }

                mx *= 4;
                x += 2;
            }
        }

        /**
         * Draw a vertical line at x from my to my+1
         * @param x0 start value of current floor
         * @param x1 start value of previous floor
         * @param my current floor
         */
        void vline(double x0, double x1, int my, Color color) {

            int ix0 = scales.sx.pix(x0);
            int ix1 = scales.sx.pix(x1);

            int iy0 = scales.sy.pix(my+1);
            int iy1 = scales.sy.pix(my);

            g.setColor(color);
            g.drawLine(ix0, iy0, ix1, iy1);
        }


        /**
         * Draw a horizontal line.
         * @param x value of current floor.
         * @param my current floor.
         */
        void hline(double x, int my) {
            if(x<xl)
                x = xl;
            int ix = scales.sx.pix(x);
            int lx = scales.sx.len();

            int iy = scales.sy.pix(my);

            g.setColor(Color.RED);
            g.drawLine(ix, iy, lx, iy);
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

    int km(long m) {
        int k = 0;

        while(m>1) {
            ++m;
            while(m%2==0) {
                m /= 2; m*= 3; ++k;
            }
            --m;
            int l2 = Long.numberOfTrailingZeros(m);
            m >>= l2;
        }

        return k;
    }
}
