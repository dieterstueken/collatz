package de.ditz.draw;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.03.25
 * Time: 23:29
 */
public class Legend2D {

    final Legend lx;
    final Legend ly;

    public Legend2D(Scale2D scales) {
        lx = Legend.of(scales, true);
        ly = Legend.of(scales, false);
    }

    public void drawTicks(Graphics g) {
        lx.drawTicks(g);
        ly.drawTicks(g);
    }
}
