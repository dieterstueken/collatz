package de.ditz.primes.main;

import de.ditz.primes.BufferedSequence;
import de.ditz.primes.PrimeFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;

public class Dump implements Predicate<BufferedSequence> {

    final PrimeFile primes;

    final long limit;

    long dups;

    public Dump(PrimeFile primes, long limit) {
        this.primes = primes;
        this.limit = limit;
    }

    @Override
    public boolean test(BufferedSequence buffer) {

        boolean done =  primes.limit() > limit;

        System.out.format("%5d %,10d %,8d %,5.1f%%\n", buffer.base, buffer.limit(), buffer.count(), 100D * buffer.dups() / buffer.size());

        if(done) {
            System.out.println();
            long[] stat = primes.stat();
                System.out.println(Arrays.toString(stat));
        }

        return done;
    }


    public static void main(String... args) throws IOException {

        //BufferedSequence.debug = -1;

        try (PrimeFile primes = PrimeFile.create(new File("primes.dat"))) {
            primes.sieve().grow(new Dump(primes, 200000));
            primes.dump("primes.txt");
        }
    }
}
