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


    static final double L15 = Math.log(1.5);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        double xl = scales.sx.lower();
        double xh = scales.sx.upper();

        double yl = scales.sy.lower();
        double yh = scales.sy.upper();

        // horizontal path
        for(int ky = (int) Math.ceil(yl); ky<yh; ++ky) {

            double kl = Math.pow(1.5, ky+xl);

            for(long k = (long) Math.ceil(kl); k<1000000; ++k) {
                double x = Math.log(k)/L15 - ky;
                if(x>xh)
                    break;

                int ix = scales.sx.pix(x);
                int iy = scales.sy.pix(ky);

                g.drawRect(ix-1, iy+1,3,3);
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
