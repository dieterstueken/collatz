package de.ditz.draw;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.03.25
 * Time: 23:44
 */
public class Pane extends JPanel {

    final Scale2D scales;

    public Pane(double dpu) {
        scales = new Scale2D(this::getWidth, this::getHeight, dpu);

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(border);

        MouseAdapter adapter = mouseAdapter();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    void pan(int ix, int iy) {
        scales.sx.pan(ix);
        scales.sy.pan(iy);
    }

    void zoom(int ix, int iy, double fx, double fy) {
        scales.sx.zoom(fx, ix);
        scales.sy.zoom(fy, iy);
    }

    MouseAdapter mouseAdapter() {
          return new MouseAdapter() {

                int ix, iy;

                void setPos(MouseEvent e) {
                      ix = e.getX();
                      iy = e.getY();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                      int m = e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK;
                      if(m!=0) {
                            setPos(e);
                      }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                      int m = e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK;
                      if(m!=0) {
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
                      if((m & InputEvent.BUTTON1_DOWN_MASK)==0) {
                          double factor = 1 - e.getPreciseWheelRotation() / 10;
                          double fx = (m & InputEvent.CTRL_DOWN_MASK)==0 ? factor : 1.0;
                          double fy = (m & InputEvent.SHIFT_DOWN_MASK)==0 ? factor : 1.0;

                          zoom(e.getX(), e.getY(), fx, fy);
                          repaint();
                      }
                }
          };
    }
}
