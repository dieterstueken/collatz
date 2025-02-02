package de.ditz.collatz;

import java.math.BigInteger;
import java.util.Arrays;

public class PowL {

    public static void main(String[] args) {
        Integer hit = args.length>0 ? Integer.parseInt(args[0]) : null;
        new PowL(hit, 10).run(70);
    }

    final BigInteger I1 = BigInteger.valueOf(1);
    final BigInteger I3 = BigInteger.valueOf(3);

    BigInteger p3 = BigInteger.valueOf(1);
    final int[] pl;

    final Integer hit;
    final int width;

    public PowL(Integer hit, int width) {
        this.hit = hit;
        this.width = width;
        this.pl = new int[width];

        head();
    }

    void head() {

        System.out.print("   ");
        for(int l=1; l<width+1; ++l) {
            System.out.format(fmt(l), l);
        }
        System.out.println();
    }

    void run(int lines) {
        for(int k=0; k<lines; ++k) {
            line(k);
            p3 = p3.multiply(I3);
        }
    }


    void line(int k) {
        System.out.format("%2d ", k);

        Arrays.fill(pl, 0);
        BigInteger p3n = p3.subtract(I1);
        BigInteger p32 = p3.add(p3);
        int nl = 0;
        for(int n=1; n<2<<width; n +=2, p3n = p3n.add(p32)) {
            int l = p3n.getLowestSetBit();
            if(l<0)
                continue;

            if(l>width)
                continue;
            
            if(pl[l-1]==0) {
                pl[l-1] = n;
                ++nl;
                if(nl>=width)
                    break;
            }
        }

        for(int n=0; n<width; ++n) {
            int l = pl[n];
            System.out.format(fmt(n+1),l);
        }

        System.out.println();
    }

    static String fmt(int i) {
        if(i<3) return "%2d";
        if(i<6) return "%3d";
        if(i<8) return "%4d";
        if(i<12) return "%5d";
        if(i<16) return "6d";
        return "%7d";
    }
}
