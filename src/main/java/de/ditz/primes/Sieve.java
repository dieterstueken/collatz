package de.ditz.primes;

import java.io.PrintStream;
import java.util.BitSet;
import java.util.function.LongConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.06.20
 * Time: 19:50
 */
public class Sieve {

    final BitSet sieve = new BitSet();

    long base = 0;

    Sieve reset(long base, int size) {
        this.base = base;
        sieve.clear();
        sieve.set(0, size);
        return this;
    }

    private int i0(long prime) {
        long p2 = 2*prime;
        long k = (base - prime + p2-1)/p2;
        long index = (p2*k + prime - base)/2;
        return (int) Math.min(index, Integer.MAX_VALUE);
    }

    boolean sieve(long prime) {

        if(prime*prime>2*sieve.length())
            return false;

        // odd numbers only
        if (prime > 2) {
            int len = sieve.length();
            for (int index = i0(prime); index < len; index += prime) {
                sieve.clear(index);
            }
        }

        return true;
    }

    void extract(LongConsumer consumer) {

        for(int i=sieve.nextSetBit(0); i>=0; i=sieve.nextSetBit(i+1)) {
            long prime = base + 2*i;
            consumer.accept(prime);
            sieve(prime);
        }
    }

    public void stat(PrintStream out) {
        int l = sieve.length();
        int n = sieve.cardinality();
        out.format("%d %d %.1f\n", l, n, 1.0 * l/n);
    }

}
