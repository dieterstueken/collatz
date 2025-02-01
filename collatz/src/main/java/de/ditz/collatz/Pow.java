package de.ditz.collatz;

import java.math.BigInteger;

public class Pow {

    static final BigInteger I1 = BigInteger.valueOf(1);
    static final BigInteger I3 = BigInteger.valueOf(3);

    public static void main(String[] args) {

        Integer hit = args.length>0 ? Integer.parseInt(args[0]) : null;

        System.out.print("    ");
        for(int k=10; k<72; k += 10) {
            System.out.format("%10d", k);
        }
        System.out.println();
        
        BigInteger p3 = BigInteger.valueOf(1);
        for(int i=1; i<33; ++i) {
            BigInteger p3k = p3.subtract(I1);
            System.out.format("%2d ", i);
            for(int k=1; k<72; ++k) {
                int l = p3k.getLowestSetBit();
                if(k%2==1) {
                    if(hit!=null && l!=hit)
                        System.out.print("  ");
                    else if (l > 1)
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
