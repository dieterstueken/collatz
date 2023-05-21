package de.ditz.zeta

import org.kotlinmath.Complex
import org.kotlinmath.complex
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.WindowConstants

/**
 * Calculates the number of iterations from which the modulus of the iterated
 * value is greater than 2 or <code>max</code> if this is not the case after
 * <code>max</code> iterations
 * @return number of iterations
 */
fun mandelbrot(z0: Complex, max: Int): Int {
    var z = z0
    repeat(max) {
        if (z.mod > 2.0) return it
        z = z * z + z0
    }
    return max
}

fun main() {
    with (JFrame()) {
        contentPane.layout = BorderLayout()

        val source = object : ImageSource {
            override fun rgb(x: Double, y: Double): Int {
                val gray = 255 - mandelbrot(complex(x, y), 255)
                val color = Color(gray / 2, gray * gray % 256, gray * gray * gray % 256)
                //return 0x01010100 * (gray%256) + 0xff
                return color.getRGB()
            }
        };
        val size = 512;
        
        val view = View(source, 3.0/size);
        view.setPreferredSize(Dimension(size, size))
        contentPane.add(view, BorderLayout.CENTER)
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        pack()
        isVisible = true
    }
}