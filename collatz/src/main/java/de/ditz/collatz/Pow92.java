package de.ditz.collatz;

import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.02.25
 * Time: 19:03
 */
public class Pow92 {

    public static BigInteger I9 = BigInteger.valueOf(9);

    static final Digitizer dig = new Digitizer(".12", true) ;

    public static void main(String ... args) {
        BigInteger p9 = I9;

        for(int i=1; i<10; ++i) {
            BigInteger q = p9.shiftRight(i);
            StringBuilder binary = dig.digits(q, 64);

            System.out.format("%3d %,15d %s\n", i, q.longValue(), binary);

            p9 = p9.multiply(I9);
        }
    }
}
