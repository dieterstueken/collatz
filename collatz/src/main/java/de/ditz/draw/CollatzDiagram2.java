package de.ditz.draw;

import java.awt.*;

import static de.ditz.draw.CollatzDiagram.open;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 15:58
 */
public class CollatzDiagram2 extends AbstractDiagram {

    public static void main(String ... args) {
        open(CollatzDiagram2::new);
    }

    public CollatzDiagram2(Scale2D scales) {
        super(scales);
    }

    public void paint2D(Graphics2D g) {

        final double xl = scales.sx.lower();
        final double xh = scales.sx.upper();

        final double yl = scales.sy.lower();
        final double yh = scales.sy.upper();
    }
}
