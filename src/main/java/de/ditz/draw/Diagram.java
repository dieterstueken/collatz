package de.ditz.draw;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.DoubleUnaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.07.21
 * Time: 18:18
 */
public class Diagram extends JPanel {

      final DoubleUnaryOperator function;

      Scale sx = new Scale(this::getWidth, "Sx") {
            void drawLine(Graphics gr, int iy, int ix0, int ix1) {
                  gr.drawLine(ix0, iy, ix1, iy);
            }

            void drawLabel(Graphics g, String label, int iy) {
                  g.drawString(label, 5, iy);
            }
      };

      Scale sy = new Scale(this::getHeight, "Sy") {
            void drawLine(Graphics gr, int ix, int iy0, int iy1) {
                  gr.drawLine(ix, iy0, ix, iy1);
            }

            void drawLabel(Graphics g, String label, int ix) {
                  int iy = mirror(5);
                  g.drawString(label, ix, iy);
            }

            int mirror(int iy) {
                  return getHeight()-iy;
            }
      };

      public Diagram(DoubleUnaryOperator function) {
            this.function = function;

            Border border = BorderFactory.createLineBorder(Color.BLACK);
            setBorder(border);

            MouseAdapter adapter = mouseAdapter();
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
            addMouseWheelListener(adapter);
      }

      int fy(int ix) {
            double x = sx.val(ix);
            double y = function.applyAsDouble(x);
            return sy.pix(y);
      }

      @Override
      public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D gr = (Graphics2D) g;

            sx.drawTicks(g, sy);
            sy.drawTicks(g, sx);

            int width = this.getWidth();
            int iy = fy(0);
            for(int ix=0; ix<width; ++ix) {
                  int ny = fy(ix+1);
                  gr.drawLine(ix, iy, ix+1, ny);
                  iy = ny;
            }
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
                              sx.pan(e.getX() - ix);
                              sy.pan(iy - e.getY());
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
                              double factor = 1 + e.getPreciseWheelRotation() / 10;

                              if((m & InputEvent.CTRL_DOWN_MASK)==0)
                                    sx.zoom(factor, e.getX());

                              if((m & InputEvent.SHIFT_DOWN_MASK)==0)
                                    sy.zoom(factor, e.getY());

                              repaint();
                        }
                  }
            };
      }
}
