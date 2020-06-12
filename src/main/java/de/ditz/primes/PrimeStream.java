package de.ditz.primes;

import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11.06.20
 * Time: 18:42
 */
public interface PrimeStream {

    long lastPrime();

    long forEachPrime(long index, LongPredicate until);

    default long forEachPrime(LongPredicate until) {
        return forEachPrime(0, until);
    }
}
