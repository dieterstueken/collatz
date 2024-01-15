package de.ditz.primes;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.06.20
 * Time: 20:13
 */
public class Factors {

    long base;

    int count = 0;

    Factors(long base) {
        this.base = base;
    }

    public Integer count(long prime) {
        if((base%prime) == 0) {
            ++count;
            do {
                base /= prime;
            } while((base%prime) == 0);
        }

        return base/prime < prime ? count : null;
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = PrimeFile.open(file)) {
            for(long l=2; l<602; ++l) {
                if((l%30)==2)
                    System.out.format("\n%5d:", l);

                Factors factors = new Factors(l);
                int count = primes.forEach(factors::count);
                System.out.format("%2d", count);
            }
            System.out.println();
        }
    }
}
