package de.ditz.primes;

import java.io.PrintStream;
import java.util.BitSet;
import java.util.function.LongUnaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.06.20
 * Time: 19:50
 */
public class Sieve {

    final BitSet sieve = new BitSet();

    long base = 0;

    long done = 0;

    Sieve reset(long base, int size) {
        if(base<0)
            throw new IllegalArgumentException("negative base");

        this.base = base;
        this.done = 0;
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

    public long sieve(PrimeStream primes) {
        long start = done;

        // repeat until stable while adding primes in parallel
        long tmp;
        do {
            tmp = done;
            done = primes.forEachPrime(tmp, this::sieve);
        } while(done>tmp);

        return done - start;
    }

    boolean sieve(long prime) {

        // odd primes only
        if(prime<3)
            return true;

        long limit = base + 2*sieve.length();

        if(prime*prime>limit)
            return false;

        int len = sieve.length();
        for (int index = i0(prime); index < len; index += prime) {
            sieve.clear(index);
        }

        return true;
    }

    public int extract(LongUnaryOperator consumer) {
        int len = sieve.length();
        int count = 0;
        for(int i=sieve.nextSetBit(0); i>=0; i=sieve.nextSetBit(i+1)) {
            long prime = base + 2*i;
            done = consumer.applyAsLong(prime);
            ++count;
            // propagate
            for(long j = prime+i; j<len; j+=prime) {
                sieve.clear((int)j);
            }
        }

        return count;
    }

    public void stat(PrintStream out) {
        int l = sieve.length();
        int n = sieve.cardinality();
        out.format("%d %d %.1f\n", l, n, 1.0 * l/n);
    }

}
