package de.ditz.primes;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.06.20
 * Time: 00:27
 */
public class PerforatedSieve extends Sieve {


    static final int SIZE = 3*5*7*11*13*17;

    private static final BitSet TEMPLATE = new Sieve() {
            {
                sieve.set(0, SIZE);
                sieve(3);
                sieve(5);
                sieve(7);
                sieve(11);
                sieve(13);
                sieve(17);
            }
        }.sieve;

    PerforatedSieve(long index) {
        reset(index);
    }
    
    PerforatedSieve() {
        this(0);
    }

    protected PerforatedSieve reset(long index) {

        long base = 19 + 2*SIZE*index;
        if(base != this.base) {
            this.base = base;
            sieve.clear();
            sieve.or(TEMPLATE);
        }

        return this;
    }

    boolean sieve(long prime) {
        if (prime > 17) {
            return super.sieve(prime);
        } else
            return true;
    }
    

    public static void main(String ... args) {
        Sieve sieve = new PerforatedSieve();

        sieve.stat(System.out);

        sieve.extract(p->{});

        sieve.stat(System.out);
    }
}
