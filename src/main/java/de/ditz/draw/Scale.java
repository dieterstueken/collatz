package de.ditz.draw;

import java.awt.*;
import java.util.function.DoubleConsumer;
import java.util.function.IntSupplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.07.21
 * Time: 21:09
 */
abstract class Scale {

    final String name;

    double origin = 0;
    final IntSupplier len;

    double scale = 1.0 / 256;

    Scale(IntSupplier len, String name) {
        this.name = name;
        this.len = len;
    }

    @Override
    public String toString() {
        return String.format("%s[%d]", name, len());
    }

    int pix(double value) {
        if(Double.isNaN(value))
            return 0;

        double pix = (value - origin) / scale;
        pix = Math.max(pix, Short.MIN_VALUE);
        pix = Math.min(pix, Short.MAX_VALUE);
        pix = Math.rint(pix);
        return mirror((int) pix);
    }

    int len() {
        return this.len.getAsInt();
    }

    double val(int pix) {
        return origin + mirror(pix) * scale;
    }

    int mirror(int pix) {
        return pix;
    }

    void zoom(double f, int pix) {
        double center = val(pix);
        scale *= f;
        origin = center - mirror(pix) * scale;
    }

    void pan(int pix) {
        origin -= pix * scale;
    }

    void ticks(double step, DoubleConsumer ticker) {
        int count = (int) Math.ceil(len() * scale / step);

        // 5 pixel minimum
        if(5*count>len())
            return;

        double start = step * Math.ceil(origin/step);

        for(int i=0; i<count; ++i) {
            ticker.accept(start + i*step);
        }
    }

    void drawTicks(Graphics g, Scale other) {
        int len = len();
        double range = len * scale;
        double step = Math.pow(10, Math.floor(Math.log10(range)));

        ticks(step, x -> drawLine(g, other, x, true));
        ticks(step/10, x -> drawLine(g, other, x, false));
    }

    void drawLine(Graphics g, Scale other, double pos, boolean major) {
        int pix = pix(pos);
        if(pix>=0 && pix<=len()) {
            other.drawLine(g, pix, major);
            if(major)
                other.drawLabel(g, pos, pix);
        }
    }

    void drawLabel(Graphics g, double value, int ix) {
        String label = String.format("%.1f", value);
        drawLabel(g, label, ix);
    }

    abstract void drawLabel(Graphics g, String label, int ix);

    void drawLine(Graphics g, int pix, boolean major) {
        int len = major ? len() : 5;
        Color saved = g.getColor();
        g.setColor(Color.lightGray);
        drawLine(g, pix, mirror(0), mirror(len));
        g.setColor(saved);
    }

    abstract void drawLine(Graphics g, int pix, int iy0, int iy1);
}
