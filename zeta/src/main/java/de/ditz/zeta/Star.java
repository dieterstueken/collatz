package de.ditz.zeta;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.05.23
 * Time: 11:20
 */
public class Star implements ImageSource {

    static void main(String ... args) {
        new Star().open(3.0);
    }

    public int rgb(double x, double y) {
        double rho = Math.hypot(x, y);
        if(rho<1)
            return Color.GRAY.getRGB();

        //if(rho<0.5)
        //    return Color.HSBtoRGB(0, 0, 1);

        float phi = (float)(Math.toDegrees(Math.atan2(x,y)));
        int i = (int)((phi+360)/180.0*50)%2;

        return (i==0 ? Color.BLACK : Color.WHITE).getRGB();
    }
}
