package de.ditz.draw;

import java.awt.*;
import java.util.function.DoubleUnaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:54
 */
public class FunctionPainter implements Paint2D {

    final DoubleUnaryOperator function;

    final Scale2D scales;

    public FunctionPainter(Scale2D scales, DoubleUnaryOperator function) {
        this.function = function;
        this.scales = scales;
    }

    int fy(int ix) {
          double x = scales.sx.val(ix);
          double y = function.applyAsDouble(x);
          return scales.sy.pix(y);
    }

    public void paint2D(Graphics2D g) {

        int width = scales.sx.len();
        int iy = fy(0);
        Graphics2D gr = (Graphics2D) g;

        for(int ix=0; ix<width; ++ix) {
              int ny = fy(ix+1);
              gr.drawLine(ix, iy, ix+1, ny);
              iy = ny;
        }
    }
}
