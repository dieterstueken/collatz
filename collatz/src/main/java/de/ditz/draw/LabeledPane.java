package de.ditz.draw;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.07.21
 * Time: 18:18
 */
public class LabeledPane extends Pane {

      final Legend2D legends;

      public LabeledPane(double dpu) {
            super(dpu);
            legends = new Legend2D(scales);
      }

      public LabeledPane() {
            this(256);
      }

      @Override
      public void paintComponent(Graphics g) {
            super.paintComponent(g);
            paint2D((Graphics2D) g);
      }

      public void paint2D(Graphics2D g) {
            legends.drawTicks(g);
      }
}
