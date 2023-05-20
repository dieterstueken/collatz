package de.ditz.primes;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.06.20
 * Time: 20:13
 */
public class Prod {

    static final double L2 = Math.log(2);

    long last = 1;

    int j=10;

    long count = 0;

    double sum = 0;

    public boolean process(long prime) {
        last = prime;

        if((prime>>j)>0) {
            show(prime);
            j+=2;
        }

        ++count;
        sum += log2(prime);

        return false;
    }

    static double log2(double v) {
        return Math.log(v) / L2;
    }

    void show(long prime) {

        double lp = log2(prime);

        System.out.format("%5.1f %,20d %,15d = %,3.3f %,20.1f = %,3.3f bpp: %,3.2f%%\n",
                lp, prime,
                count, lp-log2(count),
                sum,  lp-log2(sum),
                100*sum/count/lp);
    }

    void process(PrimeFile primes) {
        primes.forEach(5, this::process);
        show(last);
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");

        try(PrimeFile primes = PrimeFile.open(file)) {
            System.out.format("total: %,16d\n", primes.size());
            new Prod().process(primes);
        }
    }
}
