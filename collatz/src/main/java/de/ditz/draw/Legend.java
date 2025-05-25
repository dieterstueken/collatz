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

    // if false use yx
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

    public void drawTicks(Graphics2D g) {
        double step = Math.pow(10, Math.floor(Math.log10(scale.width())));
        ticks(g, step, true);
        ticks(g, step/10, false);
    }

    private void ticks(Graphics2D g, double step, boolean major) {
        int count = (int) Math.ceil(scale.width() / step);
        // 5 pixel minimum per tick
        if(5*count>scale.len())
            return;
        double start = step * Math.ceil(scale.x0/step);
        for(int i=0; i<count; ++i) {
            double pos = start + i*step;
            drawTick(g, pos, major);
        }
    }

    private void drawTick(Graphics2D g, double pos, boolean major) {
        int ix = scale.pix(pos);

        if(ix>=0 && ix<=scale.len()) {
            if(major) {
                drawLine(g, ix);
                drawLabel(g, pos, ix, 5);
            } else {
                drawLine(g, ix, 5);
            }
        }
    }

    private void drawLine(Graphics2D g, int ix) {
        drawLine(g, ix, other.len());
    }

    private void drawLine(Graphics2D g, int ix, int ly) {
        int iy0 = other.mirr(0);
        int iy1 = other.mirr(ly);

        Color saved = g.getColor();
        g.setColor(Color.lightGray);
        try {
            if (xy)
                g.drawLine(ix, iy0, ix, iy1);
            else
                g.drawLine(iy0, ix, iy1, ix);
        } finally {
            g.setColor(saved);
        }
    }

    private void drawLabel(Graphics2D g, double value, int ix, int iy) {

        // prevent overlap
        if(!xy && ix<25)
            return;

        iy = other.mirr(iy);

        String label = String.format("%.1f", value);

        if(xy)
            g.drawString(label, ix, iy);
       else
            g.drawString(label, iy, ix);
    }
}
