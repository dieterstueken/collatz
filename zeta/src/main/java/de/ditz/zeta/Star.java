package de.ditz.zeta;

import javax.swing.*;
import java.awt.*;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.05.23
 * Time: 11:20
 */
public class Star {

    public static int rgb(double x, double y) {
        double rho = Math.hypot(x, y);
        if(rho<1)
            return Color.GRAY.getRGB();

        //if(rho<0.5)
        //    return Color.HSBtoRGB(0, 0, 1);

        float phi = (float)(Math.toDegrees(Math.atan2(x,y)));
        int i = (int)((phi+360)/180.0*50)%2;

        return (i==0 ? Color.BLACK : Color.WHITE).getRGB();
    }

    static final int SIZE = 512;

    public static void main(String ... args) {

        View  view = new View(Star::rgb, 3.0/SIZE);
        view.setPreferredSize(new Dimension(SIZE, SIZE));

        JFrame jf = new JFrame();

        Container contentPane = jf.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(view);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.pack();

        invokeLater(()->jf.setVisible(true));
    }
}
