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

      public LabeledPane() {
            legends = new Legend2D(scales);
      }

      @Override
      public void paintComponent(Graphics g) {
            super.paintComponent(g);
            legends.drawTicks(g);
      }
}
