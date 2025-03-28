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
    double width; // mirrored if < 0

    Scale(double x0, double width, IntSupplier len, String name) {
        this.name = name;
        this.len = len;
        this.x0 = x0;
        this.width = width;
        if(width==0)
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
        return Math.abs(width);
    }

    Scale mirror() {
        width *= -1;
        return this;
    }

    int mirror(int pix) {
        if(width<0)
            pix = len() - pix;
        return pix;
    }

    /**
     * Pixel coordinate by value.
     * @param value to transform.
     * @return nearest pixel coordinate.
     */
    int pix(double value) {
        if(Double.isNaN(value))
            return 0;

        int len = len();

        double pix = (value - x0) * len / width;
        if(width<0) {
            pix += len;
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
        if(width<0)
            pix -= len;
        return x0 + width*pix/len;
    }

    double mval(int pix) {
        return val(mirror(pix));
    }

    int mpix(double value) {
        return mirror(pix(value));
    }

    double lower() {
        return val(mirror(0));
    }

    double upper() {
        return val(mirror(len()));
    }

    double center() {
        return (val(0) + val(len()))/2;
    }

    double dpu() {
        return dpu;
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
            if(width<0)
                pix -= len;
            width /= f;
            x0 += pix * width / len * (f-1);
        } else
            throw new IllegalArgumentException("negative zoom factor");
    }

    /**
     * Pan by pix.
     * @param pix to pan
     */
    void pan(int pix) {
        x0 -= pix * width / len();
    }
}
