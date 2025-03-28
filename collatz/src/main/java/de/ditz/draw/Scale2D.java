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

    Scale2D(IntSupplier width, IntSupplier height, double dpu) {
        sx = new Scale(0, dpu, width, "Sx");
        sy = new Scale(0, dpu, height, "Sy").mirror();
    }
}
