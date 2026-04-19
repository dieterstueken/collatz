package de.ditz.zeta;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.05.23
 * Time: 11:20
 */
public class Phi implements ImageSource {

    static void main(String ... args) {
        new Phi().open(3);
    }

    public int color(double x, double y) {
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

    public int rgb(double rho, double phi) {
        double x = rho * Math.cos(phi);
        double y = rho * Math.sin(phi);
        return color(x-1, y);
    }
}
