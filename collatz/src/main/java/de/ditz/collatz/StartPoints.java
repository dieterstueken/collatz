package de.ditz.collatz;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.05.25
 * Time: 18:37
 */
public class StartPoints {

    public static void main(String ...args) {
        long km = 3;
        for(long i=3; i<(1L<<32); i+=2) {
            int lm = lm(i);
            if(lm>km) {
                km = lm;
                double d = Math.log(lm)/Math.log(i);
                System.out.format("%,15d %4d %f\n", i, lm, d);
            }
        }
    }

    static int lm(long m) {
        int l = 0;

        while(m>1) {
            ++m;
            while(m%2==0) {
                m /= 2; m*= 3;
            }
            --m;
            int l2 = Long.numberOfTrailingZeros(m);
            m >>= l2;
            l += l2;
        }

        return l;
    }
}
