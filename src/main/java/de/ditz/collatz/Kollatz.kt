package de.ditz.collatz

import org.gciatto.kt.math.BigInteger

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11.04.20
 * Time: 17:55
 */
abstract class Kollatz(val j: BigInteger, val m: Int, val step : Int) {

    companion object {
        operator fun Int.times(factor: BigInteger) = BigInteger.of(this).times(factor)
        operator fun BigInteger.plus(value : Int) = this.plus(BigInteger.of(value))
        operator fun BigInteger.minus(value : Int) = this.minus(BigInteger.of(value))

        val B1 = BigInteger.of(1)
        val B2 = BigInteger.of(2)
        val B3 = BigInteger.of(3)
    }

    val k get() = B3 *j+m

    val n get() = B2 *k+ B1

    abstract val succ : Kollatz

    val pred: Kollatz? by lazy {
        if (m == 1)
             succ
        else {
            val even = m!=0
            // k = even ? 2*j+1 : 4*j
            val k = if(even) j.shl(1).setBit(0) else j.shl(2)

            val dr = k.divideAndRemainder(B3)
            val step = if(even) 0 else 1

            object : Kollatz(dr[0], dr[1].toIntExact(), step) {
                override val succ = this@Kollatz
            }
        }
    }
}