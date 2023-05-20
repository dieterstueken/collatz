package de.ditz.zeta

import org.kotlinmath.Complex
import org.kotlinmath.complex
import java.awt.Color
import java.awt.FlowLayout
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants
import kotlin.system.measureTimeMillis


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
    // ApacheComplex.activate()
    // LazyModArgComplex.activate()
    // CachingModArgComplex.activate()

    val n = 512 // creates an n x n image
    val bufferedImage = BufferedImage(n, n, TYPE_INT_RGB)
    val xc = -0.5 // center
    val yc = 0.0  // center
    val size = 2.0 // scaling
    val max = 255 // maximum number of iterations

    val time = measureTimeMillis {
        for (i in 0 until n) {
            for (j in 0 until n) {
                val x0 = xc - size / 2 + size * i / n
                val y0 = yc - size / 2 + size * j / n
                val z0 = complex(x0, y0)
                val gray = max - mandelbrot(z0, max)
                // Number of iterations "until z.mod > 2.0" should have different colors:
                val color = Color(gray / 2, gray * gray % 256, gray * gray * gray % 256)
                val icol = color.rgb
                bufferedImage.setRGB(i, j, icol)
            }
        }
    }
    println("Calculation time: $time")
    with (JFrame()) {
        contentPane.layout = FlowLayout()
        contentPane.add(JLabel(ImageIcon(bufferedImage)))
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack()
        isVisible = true
    }
}