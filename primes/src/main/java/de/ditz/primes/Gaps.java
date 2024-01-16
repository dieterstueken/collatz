package de.ditz.primes;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.06.20
 * Time: 20:13
 */
public class Gaps {

    long len = 0;
    long prev = 1;

    public Void run(long prime) {
        if(prime<0)
            throw new IllegalArgumentException();
        
        long g =  prime - prev;
        if(g>len) {
            System.out.format("%3d %,16d\n", g, prev);
            len = g;
        }

        prev = prime;
        return null;
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        Gaps gaps = new Gaps();
        
        try(PrimeFile primes = PrimeFile.open(file)) {
            System.out.format("total: %,16d\n", primes.size());
            primes.process(gaps::run);
        }
    }
}
