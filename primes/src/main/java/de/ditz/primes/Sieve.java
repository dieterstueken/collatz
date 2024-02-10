package de.ditz.primes;

import java.io.File;
import java.io.IOException;

public class Sieve {

    final RootBuffer root;

    final Sequence primes;

    BufferedSequence target;

    long product;

    int dups;

    public Sieve(RootBuffer root, Sequence primes) {
        this.root = root;
        this.primes = primes;
    }

    public Sieve sieve(BufferedSequence target) {
        this.target = target;
        root.fill(target);

        dups = 0;
        product = 1;
        primes.process(root.prime + 1, this::sieve);

        return this;
    }

    public int dups() {
        return dups;
    }

    BufferedSequence drop(long p) {
        Boolean dropped = target.drop(p);
        if (dropped == null)
            return target;    // terminate processing

        if (!dropped)
            ++dups;

        return null; // continue processing
    }

    BufferedSequence product(long p) {
        return drop(p * product);
    }

    BufferedSequence sieve(long prime) {

         if(product>1 && product*prime>target.offset())
           drop(product*prime);

        if(product * prime*prime >= target.limit())
            return target;

        if(product*prime>target.limit())
           return null;

        long saved = this.product;
        product *= prime;

        long start = (target.offset() + product - 1) / product;
        start = Math.max(start, prime+1);
        primes.process(start, this::product);

        primes.process(prime, this::sieve);

        product = saved;

        // continue with further primes.
        return null;
    }

    public static void main(String... args) throws IOException {

        //PrimeFile.ROOT = 5;
        BufferedSequence.debug = -1;

        try (PrimeFile primes = PrimeFile.create(new File("primes.dat"))) {

            while (primes.size() < 1024 * 1024 * 4) {
                primes.grow();
                if((primes.size()%100)==0)
                    PrimeFile.log(primes);
            }

            System.out.println();
            PrimeFile.log(primes);
        }
    }
}
