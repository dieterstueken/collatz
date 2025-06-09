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
    static final int DY = 16;

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

        long n = ((long) p2(xl-1));
        if(n<0)
            n=0;

        double nh = p2(Math.min(xh-1, 63));

        for(; n<nh; ++n) {
            long m = 2*n+1;
            
            double x = l2(m);
            double y = lm(m) - x;
            double z = l2(m+1);

            if(y<yl)
                continue;

            long m1 = 3*(m+1)/2-1;
            double x1 = l2(m1);
            double y1 = y + x - x1;
            double z1 = l2(m1+1);

            if(y1>yh)
                continue;

            drawLine(g, x, y, z, x1, y1, z1);
        }
    }

    void drawLine(Graphics2D g,
                  double x0, double y0, double z0,
                  double x1, double y1, double z1) {
        int ix = scales.sx.pix(x0);
        int iy = scales.sy.pix(y0);
        int iz = scales.sx.pix(z0);
        int kx = scales.sx.pix(x1);
        int ky = scales.sy.pix(y1);
        int kz = scales.sx.pix(z1);

        int dy = scales.sy.pix(0) - scales.sy.pix(0.3);

        g.setColor(Color.RED);
        g.drawLine(ix, iy, scales.sx.len(), iy);

        g.setColor(Color.BLACK);
        g.drawLine(ix, iy, iz, iy-dy);

        g.setColor(Color.BLUE);
        g.drawLine(iz, iy-dy, kz, ky-dy);

        g.setColor(Color.BLACK);
        g.drawLine(kz, ky-dy, kx, ky);
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
