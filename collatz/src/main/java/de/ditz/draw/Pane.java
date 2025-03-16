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

    public Pane() {
        scales = new Scale2D(this::getWidth, this::getHeight);

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(border);

        MouseAdapter adapter = mouseAdapter();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
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
                            scales.pan(e.getX() - ix, e.getY() - iy);
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

                            if((m & InputEvent.CTRL_DOWN_MASK)==0)
                                  scales.sx.zoom(factor, e.getX());

                            if((m & InputEvent.SHIFT_DOWN_MASK)==0)
                                  scales.sy.zoom(factor, e.getY());

                            repaint();
                      }
                }
          };
    }
}
