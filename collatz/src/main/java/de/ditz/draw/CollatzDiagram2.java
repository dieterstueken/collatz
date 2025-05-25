package de.ditz.draw;

import java.awt.*;

import static de.ditz.draw.CollatzDiagram.open;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:58
 */
public class CollatzDiagram2 extends AbstractDiagram {

    public static void main(String ... args) {
        open(CollatzDiagram2::new);
    }

    public CollatzDiagram2(Scale2D scales) {
        super(scales);
    }

    static final double L2 = Math.log(2);

    static double l2(double value) {
        return Math.log(value) * L2;
    }

    static double p2(double value) {
        return Math.pow(2, value);
    }

    public void paint2D(Graphics2D g) {

        final double xl = scales.sx.lower();
        final double xh = scales.sx.upper();

        final double yl = scales.sy.lower();
        final double yh = scales.sy.upper();

        double mh = p2(Math.min(xh-1, 63));

        for(long m = ((long) p2(xl-1)); m<mh; ++m) {
            double x = l2(2.0*m+1);
            double y = ln(m) - x;

            int ix = scales.sx.pix(x);
            int iy = scales.sy.pix(y);

            int kx = scales.sx.pix(x + L32);
            int ky = scales.sx.pix(y - L32);

            g.drawLine(ix, iy, kx, ky);
        }

    }

    int ln(long m) {
        int ln = 0;

        while(m>1) {
            ++m;
            while(m%2==0) {
                m /= 2; m*= 3;
            }
            --m;
            int l = Long.numberOfTrailingZeros(m);
            m >>= l;
            ln += l;
        }

        return ln;
    }
}
