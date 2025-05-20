package de.ditz.draw;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 20.05.25
 * Time: 19:04
 */
abstract public class AbstractDiagram implements Paint2D {

    static final double L32 = 1.0/Math.log(1.5);

    static double l15(double value) {
        return Math.log(value) * L32;
    }

    static double p15(double value) {
        return Math.pow(1.5, value);
    }

    protected final Scale2D scales;

    public AbstractDiagram(Scale2D scales) {
        this.scales = scales;
    }
}
