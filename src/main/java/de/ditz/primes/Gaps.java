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
public class Gaps implements LongPredicate {

    long len = 0;
    long prev = 1;

    @Override
    public boolean test(long prime) {
        if(prime<0)
            throw new IllegalArgumentException();
        
        long g =  prime - prev;
        if(g>len) {
            System.out.format("%3d %,14d\n", g, prev);
            len = g;
        }

        prev = prime;
        return false;
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        Gaps gaps = new Gaps();
        
        try(PrimeFile primes = new PrimeFile(BufferedFile.open(file.toPath()))) {
            System.out.format("total: %,14d\n", primes.size());
            primes.forEach(gaps);
        }
    }
}
