package de.ditz.draw;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:58
 */
public class CollatzDiagram1 extends LabeledPane {

    static final double L32 = 1.0/Math.log(1.5);

    static double l15(double value) {
        return Math.log(value) * L32;
    }

    static double p15(double value) {
        return Math.pow(1.5, value);
    }

    @Override
    public void paint2D(Graphics2D g) {
        super.paint2D(g);

        final double xl = scales.sx.lower();
        final double xh = scales.sx.upper();

        final double yl = scales.sy.lower();
        final double yh = scales.sy.upper();

        // k = p15(t)
        // y = 1/k

        if(yl>=1 || yh<=0)
            return;

        // pixel drawn per scanline
        int lx = 3*scales.sx.len();
        int mx = 0;

        // top down
        long k = Math.max(1, (long) Math.ceil(1/yh));
        double y = 1.0/k;
        int iy = scales.sy.pix(y);

        while(y>yl) {

            double t = l15(k);
            t += (int)Math.ceil(xl-t);

            for(; t<=xh; t += 1.0) {
                int ix = scales.sx.pix(t);
                g.fillRect(ix, iy-1, 3, 3);
                ++mx;
            }

            y = 1.0/++k;
            int ny = scales.sy.pix(y);

            if(ny==iy) {
                // same scanline
                if (mx > lx) {

                    // skip ahead
                    ny = iy + 1;
                    y = scales.sy.val(ny);
                    if(y<0)
                        break;

                    long kn = (long) Math.ceil(1/y);
                    if (kn == k)
                        throw new IllegalStateException("k unchanged");
                    k = kn;
                }
            } else {
                mx=0;
            }
            iy = ny;
        }

        mx = 0; // dummy
    }

    static void open() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.setPreferredSize(new Dimension(300, 150));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        p.add(new CollatzDiagram1());

        JFrame frame = new JFrame("Paint");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(p);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String ... args) {
        SwingUtilities.invokeLater(CollatzDiagram1::open);
    }
}
