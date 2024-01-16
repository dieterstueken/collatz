package de.ditz.primes;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.06.20
 * Time: 20:13
 */
public class Density {

    static final long BLOCK = 10000000000L;

    long count = 0;
    long block = 1;

    public Void count(long prime) {
        if(prime<0)
            throw new IllegalArgumentException();
        
        if(prime>block*BLOCK) {
            System.out.format("%5.1f %,16d %,14d\n", 1.0 * BLOCK / count, prime, count);
            block = (prime+BLOCK-1)/BLOCK;
            count = 0;
        }

        ++count;
        return null;
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        Density density = new Density();
        
        try(PrimeFile primes = PrimeFile.open(file)) {
            System.out.format("total: %,16d\n", primes.size());
            primes.process(density::count);
        }
    }
}
