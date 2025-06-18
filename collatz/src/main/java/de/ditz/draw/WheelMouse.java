package de.ditz.draw;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.05.25
 * Time: 15:01
 */
abstract public class WheelMouse extends MouseAdapter {

    final Scale2D scales;

    int ix, iy;

    public WheelMouse(Scale2D scales) {
        this.scales = scales;
    }

    abstract void repaint();

    abstract void mouseMoved(double x, double y);

    void setPos(MouseEvent e) {
        ix = e.getX();
        iy = e.getY();
    }

    void pan(int ix, int iy) {
        scales.sx.pan(ix);
        scales.sy.pan(iy);
    }

    void scale(int ix, int iy, double sx, double sy) {
        scales.sx.scale(sx, ix);
        scales.sy.scale(sy, iy);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int m = e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK;
        if (m != 0) {
            setPos(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        double x = scales.sx.val(e.getX());
        double y = scales.sy.val(e.getY());
        mouseMoved(x, y);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int m = e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK;
        if (m != 0) {
            pan(e.getX() - ix, e.getY() - iy);
            setPos(e);
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int m = e.getModifiersEx();
        if ((m & InputEvent.BUTTON1_DOWN_MASK) == 0) {
            double s = e.getPreciseWheelRotation();
            double sx = (m & InputEvent.CTRL_DOWN_MASK) == 0 ? s : 0;
            double sy = (m & InputEvent.SHIFT_DOWN_MASK) == 0 ? s : 0;

            scale(e.getX(), e.getY(), sx, sy);
            repaint();
        }
    }
};
