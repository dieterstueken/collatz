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

        for(long k = Math.max(1L, (long)Math.ceil(yl)); k<yh; ++k) {
            int iy = scales.sy.pix(k);

            double t = l15(k);
            t += (int)Math.ceil(xl-t);
            
            for(; t<=xh; t += 1.0) {
                int ix = scales.sx.pix(t);
                g.fillRect(ix, iy-1, 3, 3);
            }
        }
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
