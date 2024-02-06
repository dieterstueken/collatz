package de.ditz.primes;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 23.01.24
 * Time: 23:03
 */

/**
 * Merge additional factors of prime^n into the incoming factors.
 * Assume factors are increasing.
 */
abstract public class PowerTarget {

    final int pow;

    public PowerTarget(int pow) {
        this.pow = pow;
    }

    abstract boolean test(long start);

    protected double root(long start) {
        return StrictMath.pow(start, 1.0/pow);
    }
}
