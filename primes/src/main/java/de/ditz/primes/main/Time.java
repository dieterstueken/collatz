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
public class Time implements AutoCloseable {


    final PrimeFile primes;

    long start;

    Time(PrimeFile primes) {
        this.primes = primes;
    }

    @Override
    public void close() throws Exception {
        primes.close();
    }

    void log() {
        long elapsed = System.currentTimeMillis() - start;
        System.out.format("%.1f %d %,d %,d %,.1f%%\n", elapsed / 1000.0, primes.size(), primes.limit(), primes.count(), primes.dups());
    }

    Predicate<BufferedSequence> until(int seconds) {

        return buffer -> {
            if((primes.size()%100)==0)
                log();
            return System.currentTimeMillis() > start + 1000*seconds;
        };
    }

    public void grow(int seconds) {
        start = System.currentTimeMillis();
        primes.grow(until(seconds));
        System.out.println();
        log();
        System.out.format("total: %,16d\n", primes.size());
    }

    public static void main(String ... args) throws IOException {

        File file = File.createTempFile( "primes", ".dat");
        PrimeFile.ROOT = 23;

        try(PrimeFile primes = PrimeFile.create(file)) {
            new Time(primes).grow(30);
        }
    }

}
