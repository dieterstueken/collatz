package de.ditz.draw;

import javax.swing.*;
import java.awt.*;
import java.util.function.DoubleUnaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.07.21
 * Time: 18:39
 */
public class Painter extends JPanel {

    Painter() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5,25,25,5));
        setPreferredSize(new Dimension(300, 150));

        DoubleUnaryOperator function = x -> Math.sin(x);

        add(new Diagram(function), BorderLayout.CENTER);
    }

    void open() {

        JFrame frame = new JFrame("Paint");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        
        frame.pack();
        frame.setVisible(true);
    }
    
    
    public static void main(String ... args) {
     
        final Painter painter = new Painter();
        
        SwingUtilities.invokeLater(painter::open);
    }
}
