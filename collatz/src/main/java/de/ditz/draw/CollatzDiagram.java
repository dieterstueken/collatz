package de.ditz.draw;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:58
 */
public class CollatzDiagram implements Paint2D {

    public static void main(String ... args) {
        openFrame(CollatzDiagram::new);
    }

    static void openFrame(Function<Scale2D, Paint2D> painter) {
        SwingUtilities.invokeLater(() -> {
            Pane2D pane = Pane2D.labeled();
            pane.addPainter(painter);
            Pane2D.openFrame(pane);
        });
    }

    static final double L32 = 1.0/Math.log(1.5);

    static double l15(double value) {
        return Math.log(value) * L32;
    }

    static double p15(double value) {
        return Math.pow(1.5, value);
    }

    final Scale2D scales;

    public CollatzDiagram(Scale2D scales) {
        this.scales = scales;
    }

    public void paint2D(Graphics2D g) {

        final double xl = scales.sx.lower();
        final double xh = scales.sx.upper();

        final double yl = scales.sy.lower();
        final double yh = scales.sy.upper();

        // y = l15(k)

        long k = (long) Math.ceil(p15(Math.max(0.0, yl)));
        double y = l15(k);
        while(y<yh) {
            int iy = scales.sy.pix(y);

            double t = y + (int)Math.ceil(Math.max(0, xl)-y);
            for(; t<=xh; t += 1.0) {
                int ix = scales.sx.pix(t);
                g.fillRect(ix, iy-1, 3, 3);
            }

            y = l15(++k);
            int jy = scales.sy.pix(y);
            if(jy==iy) {
                y = scales.sy.val(iy-1);
                k = (int) Math.ceil(p15(Math.max(0.0, y)));
                y = l15(k);
            }
        }
    }
}
