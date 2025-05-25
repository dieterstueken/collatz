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
public class WheelMouse extends MouseAdapter {

    final Scale2D scales;
    final Runnable repaint;

    int ix, iy;

    public WheelMouse(Scale2D scales, Runnable repaint) {
        this.scales = scales;
        this.repaint = repaint;
    }

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
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int m = e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK;
        if (m != 0) {
            pan(e.getX() - ix, e.getY() - iy);
            setPos(e);
            repaint.run();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        repaint.run();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int m = e.getModifiersEx();
        if ((m & InputEvent.BUTTON1_DOWN_MASK) == 0) {
            double s = e.getPreciseWheelRotation();
            double sx = (m & InputEvent.CTRL_DOWN_MASK) == 0 ? s : 0;
            double sy = (m & InputEvent.SHIFT_DOWN_MASK) == 0 ? s : 0;

            scale(e.getX(), e.getY(), sx, sy);
            repaint.run();
        }
    }
};
