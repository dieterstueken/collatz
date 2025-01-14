package de.ditz.collatz;

import java.math.BigInteger;

public class Pow {

    static final BigInteger I3 = BigInteger.valueOf(3);

    public static void main(String[] args) {

        for(int i=0; i<30; ++i) {
            System.out.format("%3d", i);
            long l = 1L<<i;
            for(int k=1; k<16; ++k) {
                int k3 = k3(k*l);
                if(k3==0)
                    System.out.print("  .");
                else
                    System.out.format("%3d", k3);
            }
            System.out.println();
        }
    }

    static int k3(long n) {
        int k3 = 0;
        while(n>0) {
            long m3 = n % 3;
            if(m3==2)
                ++k3;
            else
                break;
            n /= 3;
        }

        return k3;
    }
}
