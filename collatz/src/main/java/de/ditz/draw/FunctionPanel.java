package de.ditz.draw;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.07.21
 * Time: 18:39
 */
public class FunctionPanel extends JPanel {

    DefaultBoundedRangeModel maxModel = new DefaultBoundedRangeModel(5, 0, 0, 500);

    FunctionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(300, 150));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        Pane2D pane = Pane2D.labeled();
        add(pane);
        pane.addPainter(new FunctionPainter(pane.scales, this::f));

        maxModel.addChangeListener(e -> pane.repaint());
        JSlider maxSlider = new JSlider(maxModel);

        maxSlider.setMajorTickSpacing(100);
        maxSlider.setMinorTickSpacing(10);
        maxSlider.setPaintTicks(true);
        maxSlider.setPaintLabels(true);

        maxSlider.setBorder(BorderFactory.createEmptyBorder(15,0,0,0));
        add(maxSlider);
    }

    void open() {

        JFrame frame = new JFrame("Paint");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        
        frame.pack();
        frame.setVisible(true);
    }

    double fn(int n, double x) {
        x /= n;
        x -= 1;
        x *= Math.PI;
        return Math.sin(x)/Math.atan(x);
    }

    double f(double x) {

        double f = 1;
        int max = maxModel.getValue();
        for(int i=2; i<max; ++i)
            f *= fn(i, x);

        return f;
    }

    public static void main(String ... args) {

        final FunctionPanel painter = new FunctionPanel();
        
        SwingUtilities.invokeLater(painter::open);
    }
}
