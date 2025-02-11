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

    public static void main(String ... args) {
        BigInteger p3 = I3;

        for(int i=1; i<100; ++i) {
            int len = p3.bitLength();
            StringBuilder binary = binary(p3.shiftRight(i));
            binary.delete(0, i);
            System.out.format("%3d %,15d %s\n", i, len, binary);
            p3 = p3.parallelMultiply(p3);
        }
    }

    private static StringBuilder binary(BigInteger p3) {

        StringBuilder sb = new StringBuilder();

        while(!I0.equals(p3) && sb.length()<100)  {
            int l = p3.getLowestSetBit();

            for(int i=0; i<l; ++i)
                sb.append(' ');

            sb.append('1');
            p3 = p3.shiftRight(l+1);
        }

        return sb;
    }
}
