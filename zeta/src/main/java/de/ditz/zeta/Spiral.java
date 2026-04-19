package de.ditz.zeta;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.05.23
 * Time: 11:20
 */
public class Spiral implements ImageSource {

    static void main(String ... args) {
        new Spiral().open(3.0);
    }

    public int rgb(double x, double y) {

        double r = Math.hypot(x, y);
        double phi = Math.atan2(x, y);
        double t = phi/Math.PI/2;

        t += Math.floor(r-t+0.5);
        double d = r - t;

        Color color = 20*Math.abs(d)<1 ? Color.black : Color.white;
        return color.getRGB();
    }

    @Override
    public void hit(double x, double y) {
        rgb(x, y);
    }
}
