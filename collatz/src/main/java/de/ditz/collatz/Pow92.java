package de.ditz.collatz;

import java.math.BigInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.02.25
 * Time: 19:03
 */
public class Pow92 {

    public static BigInteger I0 = BigInteger.valueOf(0);
    public static BigInteger I9 = BigInteger.valueOf(9);

    static final Digitizer dig = new Digitizer(".12", true) ;

    public static void main(String ... args) {
        BigInteger p9 = I9;

        for(int k=1; k<10; ++k) {
            BigInteger q = p9.shiftRight(k+2);
            StringBuilder binary = dig.digits(q, 64);

            System.out.format("%3d %30s %s\n", k, q, binary);

            p9 = p9.multiply(p9);
        }
    }

}
