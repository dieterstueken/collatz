package de.ditz.primes;

import java.io.File;
import java.io.IOException;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  13.06.2020 14:11
 * modified by: $
 * modified on: $
 */
public class Steps {

    int[] stat = new int[256];
    long last = 0;
    int max = 0;

    public boolean test(long prime) {
        if(last!=0) {
            int step = (int)(prime - last)/2 - 1;
            ++stat[step];
            if(step>=max)
                max = step+1;
        }

        last = prime;
        return true;
    }

    public void show() {
        for (int i = 0; i < max; i++) {
            int n = stat[i];
            System.out.format("%3d %8d\n", 2*i, n);
        }
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");
        
        PrimeFile primes = PrimeFile.open(file.toPath());

        Steps stat = new Steps();
        primes.forEachInt(0, stat::test);

        stat.show();
    }
}
