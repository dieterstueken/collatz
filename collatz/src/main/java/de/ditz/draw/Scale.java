package de.ditz.draw;

import java.util.function.IntSupplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.07.21
 * Time: 21:09
 */
public class Scale {

    final String name;

    // current length
    final IntSupplier len;

    double x0; // value at ix==x0
    double dpu; // mirrored if < 0

    Scale(double x0, double dpu, IntSupplier len, String name) {
        this.name = name;
        this.len = len;
        this.x0 = x0;
        this.dpu = dpu;
        if(dpu==0)
            throw new IllegalArgumentException("negative zoom factor");
    }

    Scale(IntSupplier len, String name) {
        this(0.0, 1.0, len, name);
    }

    @Override
    public String toString() {
        return String.format("%s[%d:%.1f-%.1f]", name, len(), val(0), val(len()));
    }

    int len() {
        return this.len.getAsInt();
    }

    double width() {
        return len()/Math.abs(dpu);
    }

    Scale mirror() {
        dpu *= -1;
        return this;
    }

    /**
     * Pixel coordinate by value.
     * @param value to transform.
     * @return nearest pixel coordinate.
     */
    int pix(double value) {
        if(Double.isNaN(value))
            return 0;

        double pix = (value - x0) * dpu;
        if(dpu<0) {
            pix += len();
        }

        pix = Math.max(pix, Short.MIN_VALUE);
        pix = Math.min(pix, Short.MAX_VALUE);
        pix = Math.rint(pix);

        return (int) pix;
    }

    /**
     * pixel to value.
     * @param pix pixel value
     * @return double value by origin and scale
     */
    double val(int pix) {
        int len = len();
        if(dpu<0)
            pix -= len;
        return x0 + pix/dpu;
    }

    /**
     * Mirror pixel value if dpu < 0 (y-axis)
     * @param pix coord
     * @return pix or len-pix
     */
    int mirr(int pix) {
        if(dpu<0)
            pix = len() - pix;
        return pix;
    }

    double mval(int pix) {
        return val(mirr(pix));
    }

    int mpix(double value) {
        return mirr(pix(value));
    }

    double lower() {
        return val(mirr(0));
    }

    double upper() {
        return val(mirr(len()));
    }

    double center() {
        return (val(0) + val(len()))/2;
    }

    /**
     * Perform a zoom at a given pixel position.
     * The value for that pixel must stay.
     * @param f zoom factor.
     * @param pix position to stay stable.
     */
    void zoom(double f, int pix) {
        if(f>0) {
            int len = len();
            if(dpu<0)
                pix -= len;
            dpu *= f;
            x0 += pix / dpu * (f-1);
        } else
            throw new IllegalArgumentException("negative zoom factor");
    }

    void scale(double step, int pix) {
        zoom(Math.pow(1.125, -step), pix);
    }

    /**
     * Pan by pix.
     * @param pix to pan
     */
    void pan(int pix) {
        x0 -= pix / dpu;
    }
}
