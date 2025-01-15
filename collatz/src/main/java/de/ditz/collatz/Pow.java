package de.ditz.collatz;

import java.math.BigInteger;

public class Pow {

    static final BigInteger I3 = BigInteger.valueOf(3);

    public static void main(String[] args) {
        BigInteger p3 = BigInteger.valueOf(1);
        for(int i=1; i<33; ++i) {
            BigInteger p3k = p3.subtract(BigInteger.valueOf(1));
            System.out.format("%2d ", i);
            for(int k=1; k<72; ++k) {
                int l = p3k.getLowestSetBit();
                if(k%2==1) {
                    if (l > 1)
                        System.out.format("%2X", l);
                    else
                        System.out.print("  ");
                }
                p3k = p3k.add(p3);
            }
            System.out.println();
            p3 = p3.multiply(I3);
        }
    }
}
