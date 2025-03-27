package de.ditz.draw;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:58
 */
public class CollatzDiagram extends LabeledPane {

    static final double L32 = 1.0/Math.log(1.5);

    static double l15(double value) {
        return Math.log(value) * L32;
    }

    static double p15(double value) {
        return Math.pow(1.5, value);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        final double xl = scales.sx.lower();
        final double xh = scales.sx.upper();

        final double yl = scales.sy.lower();
        final double yh = scales.sy.upper();

        // k = p15(t)
        // y = 1-1/k
        // k = 1/(1-y)

        if(yl>=1 || yh<=0)
            return;

        long k0 = Math.max(1, (long) Math.ceil(1/(1-yl)));
        long k = k0;
        int iy = scales.sy.pix((k-1.0)/k);

        // pixel drawn per scanline
        int lx = 3*scales.sx.len();
        int mx = 0;

        // y shrinks towards 0
        while(iy>=0) {

            double t = l15(k);
            t += (int)Math.ceil(xl-t);

            for(; t<=xh; t += 1.0) {
                int ix = scales.sx.pix(t);
                g.fillRect(ix, iy-1, 3, 3);
                ++mx;
            }

            ++k;
            double y = (k-1.0)/k;
            int ny = scales.sy.pix(y);

            if(ny==iy) {
                // same scanline
                if (mx > lx) {
                    // skip ahead
                    ny = iy - 1;
                    y = scales.sy.val(ny);
                    if (y > 1)
                        break;

                    long kn = (long) Math.ceil(1 / (1 - y));
                    if (kn <= k)
                        throw new IllegalStateException("k not growing");
                    k = kn;
                }
            } else {
                mx=0;
            }
            iy = ny;
        }

        k -= k0;
        k = 0;
    }

    static void open() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.setPreferredSize(new Dimension(300, 150));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        p.add(new CollatzDiagram());

        JFrame frame = new JFrame("Paint");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(p);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String ... args) {
        SwingUtilities.invokeLater(CollatzDiagram::open);
    }
}
