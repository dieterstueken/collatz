package de.ditz.collatz;

import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.02.25
 * Time: 19:03
 */
public class Pow32 {

    public static BigInteger I0 = BigInteger.valueOf(0);
    public static BigInteger I3 = BigInteger.valueOf(3);

    static final Digitizer dig = new Digitizer(".1", true) ;

    public static void main(String ... args) {
        BigInteger p3 = I3;

        for(int i=1; i<100; ++i) {
            int len = p3.bitLength();
            StringBuilder binary = dig.digits(p3, 64);
            System.out.format("%3d %,15d %s\n", i, len, binary);
            p3 = p3.multiply(I3);
        }
    }
}
