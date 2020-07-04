package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.06.20
 * Time: 20:13
 */
public class Density implements LongPredicate {

    static final long BLOCK = 10000000000L;

    long count = 0;
    long block = 1;

    @Override
    public boolean test(long prime) {
        if(prime<0)
            throw new IllegalArgumentException();
        
        if(prime>block*BLOCK) {
            System.out.format("%5.1f %,16d %,14d\n", 1.0 * BLOCK / count, prime, count);
            block = (prime+BLOCK-1)/BLOCK;
            count = 0;
        }

        ++count;
        return false;
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        Density density = new Density();
        
        try(PrimeFile primes = new PrimeFile(BufferedFile.open(file.toPath()))) {
            System.out.format("total: %,16d\n", primes.size());
            primes.forEach(density);
        }
    }
}
