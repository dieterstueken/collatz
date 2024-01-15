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
public class Factors implements LongPredicate {

    long base;

    int count = 0;

    Factors(long base) {
        this.base = base;
    }

    @Override
    public boolean test(long prime) {
        if((base%prime) == 0) {
            ++count;
            do {
                base /= prime;
            } while((base%prime) == 0);
        }

        return base/prime < prime;
    }

    public int count(Sequence primes) {
        primes.forEach(this);
        return count;
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = PrimeFile.open(file)) {
            for(long l=2; l<602; ++l) {
                if((l%30)==2)
                    System.out.format("\n%5d:", l);

                Factors factors = new Factors(l);
                int count = factors.count(primes);
                System.out.format("%2d", count);
            }
            System.out.println();
        }
    }
}
