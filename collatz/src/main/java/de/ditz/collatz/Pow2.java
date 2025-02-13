package de.ditz.collatz;

import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.02.25
 * Time: 19:03
 */
public class Pow2 {

    public static final BigInteger I1 = BigInteger.valueOf(1);
    public static final BigInteger I2 = BigInteger.valueOf(2);

    static final Digitizer dig = new Digitizer(".12") ;

    public static void main(String ... args) {
        BigInteger p2 = I2;

        for(int i=1; i<100; ++i) {

            int len = p2.bitLength();
            StringBuilder binary = dig.digits(p2.add(I1));
            System.out.format("%3d %3d   %s\n", i, len, binary);
            p2 = p2.multiply(I2);
        }
    }
}
