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
public class Phi {

    public static int rgb(double x, double y) {
        double rho = Math.hypot(x, y);
        //if(rho<0.5)
        //    return Color.HSBtoRGB(0, 0, 1);

        float phi = (float)(Math.toDegrees(Math.atan2(x,y)));
        if(rho>0)
            rho = Math.log(rho);

        int k = (int) Math.floor(phi);
        int n = (int) Math.floor(10*rho);

        phi += 180 * (n%2) + 120*k;

        return Color.HSBtoRGB(phi/360, 1, 1);
    }

    public static int phi(double rho, double phi) {
        double x = rho * Math.cos(phi);
        double y = rho * Math.sin(phi);
        return rgb(x-1, y);
    }

    static final int SIZE = 512;

    public static void main(String ... args) {

        View  view = new View(Phi::phi, 3.0/SIZE);
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
