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
public class Count implements LongPredicate {

    long[] prime = {1, 7, 11, 13, 17, 19, 23, 29, 31};

    int j=1;
    long limit = 30;
    long count = 0;

    @Override
    public boolean test(long prime) {

        if(prime>limit) {
            System.out.format("%,23d %2d %,20d %5.1f\n",
                    limit, this.prime[j-1], count, (double)limit*8/count/30);

            if(j>=this.prime.length)
                return true;

            limit *= this.prime[j++];
        }

        ++count;
        return false;
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");


        try(PrimeFile primes = PrimeFile.open(file)) {
            System.out.format("total: %,16d\n", primes.size());
            Count count = new Count();
            primes.forEach(5, count);
        }
    }
}
