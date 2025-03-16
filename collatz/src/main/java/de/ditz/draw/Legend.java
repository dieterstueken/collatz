package de.ditz.draw;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.03.25
 * Time: 17:57
 */
public class Legend {

    final Scale scale;
    final Scale other;

    final boolean xy;

    public Legend(Scale scale, Scale other, boolean xy) {
        this.scale = scale;
        this.other = other;
        this.xy = xy;
    }

    public static Legend of(Scale2D scales, boolean xy) {
        if(xy)
            return new Legend(scales.sx, scales.sy, true);
        else
            return new Legend(scales.sy, scales.sx, false);
    }

    void drawTicks(Graphics g) {
        double step = Math.pow(10, Math.floor(Math.log10(scale.width())));
        ticks(g, step, true);
        ticks(g, step/10, false);
    }

    void ticks(Graphics g, double step, boolean major) {
        int count = (int) Math.ceil(scale.width() / step);
        // 5 pixel minimum
        if(5*count>scale.len())
            return;
        double start = step * Math.ceil(scale.off/step);
        for(int i=0; i<count; ++i) {
            double pos = start + i*step;
            drawTick(g, pos, major);
        }
    }

    void drawTick(Graphics g, double pos, boolean major) {
        int ix = scale.pix(pos);
        if(ix>=0 && ix<=scale.len()) {
            int i0 = other.mirror(0);
            int i5 = other.mirror(5);
            int i1 = major ? other.mirror(other.len()) : i5;
            drawLine(g, ix, i0, i1);
            if(major)
                drawLabel(g, pos, ix, i5);
        }
    }

    void drawLine(Graphics g, int ix, int i0, int i1) {
        Color saved = g.getColor();
        g.setColor(Color.lightGray);
        if(xy)
            g.drawLine(ix, i0, ix, i1);
        else
            g.drawLine(i0, ix, i1, ix);
        g.setColor(saved);
    }

    void drawLabel(Graphics g, double value, int ix, int iy) {
        String label = String.format("%.1f", value);

        if(xy)
            g.drawString(label, ix, iy);
       else
            g.drawString(label, iy, ix);
    }
}
