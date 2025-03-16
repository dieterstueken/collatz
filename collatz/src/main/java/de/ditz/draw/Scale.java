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

    double off; // value at ix==x0
    double dpu; // dots per unit, mirrored if < 0

    Scale(double off, double dpu, IntSupplier len, String name) {
        this.name = name;
        this.len = len;
        this.off = off;
        this.dpu = dpu;
        if(dpu==0)
            throw new IllegalArgumentException("negative zoom factor");
    }

    Scale(IntSupplier len, String name) {
        this(0.0, 256.0, len, name);
    }

    @Override
    public String toString() {
        return String.format("%s[%d:%.1f-%.1f]", name, len(), val(0), val(len()));
    }

    int len() {
        return this.len.getAsInt();
    }

    double width() {
        return Math.abs(len()/dpu);
    }

    Scale mirror() {
        dpu *= -1;
        return this;
    }

    int mirror(int pix) {
        if(dpu<0)
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

        double pix = (value - off) * dpu;
        pix = Math.max(pix, Short.MIN_VALUE);
        pix = Math.min(pix, Short.MAX_VALUE);
        pix = Math.rint(pix);

        if(dpu<0)
            pix += len();

        return (int) pix;
    }

    /**
     * pixel to value.
     * @param pix pixel value
     * @return double value by origin and scale
     */
    double val(int pix) {
        if(dpu<0)
            pix -= len();
        return off + pix/dpu;
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

    /**
     * Perform a zoom at a given pixel position.
     * The value for that pixel must stay.
     * @param f zoom factor.
     * @param pix position to stay stable.
     */
    void zoom(double f, int pix) {
        if(f>0) {
            if(dpu<0)
                pix -= len();
            dpu *= f;
            off += pix / dpu * (f-1);
        } else
            throw new IllegalArgumentException("negative zoom factor");
    }

    /**
     * Pan by pix.
     * @param pix to pan
     */
    void pan(int pix) {
        off -= pix / dpu;
    }
}
