package de.ditz.draw;

import java.util.function.IntSupplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.03.25
 * Time: 18:29
 */
public class Scale2D {

    final Scale sx;
    final Scale sy;

    Scale2D(IntSupplier width, IntSupplier height) {
        sx = new Scale(width, "Sx");
        sy = new Scale(height, "Sy").mirror();
    }

    void pan(int ix, int iy) {
        sx.pan(ix);
        sy.pan(iy);
    }
}
