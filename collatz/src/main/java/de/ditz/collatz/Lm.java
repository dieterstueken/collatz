package de.ditz.collatz;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.07.25
 * Time: 14:24
 */
public class Lm {

    static long[] POW3 = new long[40];

    static {
        long[] POW3 = new long[40];
        long p3 = 1;
        for(int i=0; i<40; ++i) {
            POW3[i] = p3;
            p3 *= 3;
        }
        Lm.POW3 = POW3;
    }

    static int kl(long m) {
        int l = 0;
        int k = 0;
        while(m>2) {

            ++m;
            int i = Long.numberOfTrailingZeros(m);
            m >>= i;
            m *= POW3[i];
            k += i;
            --m;

            i = Long.numberOfTrailingZeros(m);
            m >>= i;
            l += i;
        }

        if(Math.max(k, l)>Short.MAX_VALUE)
            throw new IndexOutOfBoundsException();

        return (k<<16) + l;
    }
    
    public static void main(String ... args) throws FileNotFoundException {

        long max = 1L << (args.length>0 ? Long.parseLong(args[0]) : 16);

        try(PrintStream out = args.length<1 ? System.out : new PrintStream(args[1])) {

            for(long m = 1; m<max; m+=2){
                int kl = kl(m);
                final int k = kl >> 16;
                final int l = kl & 0xffff;

                out.format("%d %3d %3d   %,d\n", m%3, k, l, m);
            }
        }
    }
}
