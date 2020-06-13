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
public class Bytes {

    int[] stat = new int[256];
    int[] bits = {1, 7, 11, 13, 17, 19,23, 29};
    int[] masks = new int[15];

    int mask = 0;
    int count = -1;

    public Bytes() {
        for (int i = 0; i < bits.length; i++) {
            int bit = bits[i];
            masks[(bit - 1) / 2] = 1 << i;
        }
    }

    public boolean test(long prime) {

        int n = (int)(prime/30);
        if(n>count) {
            if(count>=0)
                ++stat[mask];

            count = n;
            mask = 0;
        }

        int k = (int)(prime%30);
        k = (k-1)/2;
        mask |= masks[k];

        return true;
    }

    public void show() {
        for (int i = 0; i < 256; i++) {
            int n = stat[i];
            System.out.format("%02x %8d\n", i, n);
        }
    }

    public static void main(String ... args) throws IOException {
        File file = new File(args.length > 0 ? args[0] : "primes.dat");
        
        PrimeFile primes = PrimeFile.open(file.toPath());

        Bytes stat = new Bytes();
        primes.forEachInt(0, stat::test);

        stat.show();
    }
}
