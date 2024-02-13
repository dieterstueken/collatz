package de.ditz.primes.main;

import de.ditz.primes.BufferedSequence;
import de.ditz.primes.PrimeFile;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11.02.24
 * Time: 14:20
 */
public class Time implements Predicate<BufferedSequence> {

    final PrimeFile primes;

    long start = System.currentTimeMillis();

    final double duration;

    long dups = 0;

    Time(PrimeFile primes, double duration) {
        this.primes = primes;
        this.duration = duration;
    }

    @Override
    public boolean test(BufferedSequence buffer) {

        double elapsed = (System.currentTimeMillis() - start) / 1000D;

        boolean done = elapsed >= duration;

        dups += buffer.dups();

        if ((primes.size() % 100) == 0 || done) {
            double duprate = 100D * buffer.dups() / buffer.size();

            System.out.format("%4.1f %5d %,14d %,12d %4.1f%%\n", elapsed, primes.size(),
                    primes.limit(), primes.count(), duprate);

            if(done) {
                System.out.println();
                System.out.format("total: %,16d %,.1f%%\n", primes.size(), 100D * dups / primes.size());
            }
        }

        return done;
    }

    public static void main(String ... args) throws IOException {

        File file = File.createTempFile( "primes", ".dat");
        //PrimeFile.ROOT = 23;

        try(PrimeFile primes = PrimeFile.create(file)) {
            primes.sieve().grow(new Time(primes, 30));
        }
    }
}
