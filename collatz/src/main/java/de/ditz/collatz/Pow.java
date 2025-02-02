package de.ditz.collatz;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

public class Pow {

    public static void main(String[] args) {
        Integer hit = args.length>0 ? Integer.parseInt(args[0]) : null;
        new Pow(hit, 72).run(33);
    }

    final List<BigInteger> BI;

    final BigInteger I1 = BigInteger.valueOf(1);
    final BigInteger I3 = BigInteger.valueOf(3);

    BigInteger p3 = BigInteger.valueOf(1);

    final Integer hit;
    final int width;

    public Pow(Integer hit, int width) {
        this.hit = hit;
        this.width = width;

        BI = IntStream.range(0, width).mapToObj(BigInteger::valueOf).toList();

        head();
    }

    BigInteger bi(int i) {
        return BI.get(i);
    }

    void head() {

        System.out.print("   ");
        for(int k=9; k<width; k += 10) {
            System.out.format("%5d", k);
        }
        System.out.println();

    }

    void run(int lines) {
        for(int i=0; i<lines; ++i) {
            line(i);
            p3 = p3.multiply(I3);
        }
    }

    void line(int i) {
        System.out.format("%2d ", i);

        for(int k=1; k<width; k+=2) {
            BigInteger p3k = p3.multiply(bi(k)).subtract(I1);
            
            int l = p3k.getLowestSetBit();
            if(l<1 || hit!=null && hit!=l)
                System.out.print((k%10)==9?"|":" ");
            else
                System.out.format("%1x", l);
        }

        System.out.println();
    }
}
