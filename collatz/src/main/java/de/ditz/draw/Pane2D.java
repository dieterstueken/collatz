package de.ditz.draw;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.03.25
 * Time: 23:44
 */
public class Pane2D extends JPanel {

    final Scale2D scales;

    final List<Paint2D> painters = new ArrayList<>();

    public Pane2D(double dpu) {
        scales = new Scale2D(this::getWidth, this::getHeight, dpu);

        Border border = BorderFactory.createLineBorder(Color.BLACK);
        setBorder(border);

        MouseAdapter adapter = mouseAdapter();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    @Override
    public void paintComponent(Graphics g) {
          super.paintComponent(g);
          paint2D((Graphics2D) g);
    }

    private void paint2D(Graphics2D g) {
          for (Paint2D painter : painters) {
                painter.paint2D((Graphics2D) g);
          }
    }

    static Pane2D open() {
        return new Pane2D(256);
    }

    static Pane2D labeled() {
        return open().addLabels();
    }

    public Pane2D addLabels() {
        return addPainter(Legend2D::new);
    }

    public Pane2D addPainter(Paint2D painter) {
        painters.add(painter);
        return this;
    }

    public Pane2D addPainter(Function<Scale2D, Paint2D> painter) {
           return addPainter(painter.apply(scales));
    }

    static void open(Function<Scale2D, Paint2D> painter) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        p.setPreferredSize(new Dimension(300, 150));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        Pane2D pane = Pane2D.labeled();
        pane.addPainter(painter);
        p.add(pane);

        JFrame frame = new JFrame("Paint");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(p);

        frame.pack();
        frame.setVisible(true);
    }

    void pan(int ix, int iy) {
        scales.sx.pan(ix);
        scales.sy.pan(iy);
    }

    void scale(int ix, int iy, double sx, double sy) {
        scales.sx.scale(sx, ix);
        scales.sy.scale(sy, iy);
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
              public void mouseMoved(MouseEvent e) {
                  movedTo(e.getX(), e.getY());
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
                          double s = e.getPreciseWheelRotation();
                          double sx = (m & InputEvent.CTRL_DOWN_MASK)==0 ? s : 0;
                          double sy = (m & InputEvent.SHIFT_DOWN_MASK)==0 ? s : 0;

                          scale(e.getX(), e.getY(), sx, sy);
                          repaint();
                      }
                }
          };
    }

    protected void movedTo(int x, int y) {
    }
}
