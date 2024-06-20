package de.ditz.collatz;

import java.math.BigInteger;
import java.util.List;
import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

public class Blocks {
    static final BigInteger THREE = BigInteger.valueOf(3);

    final int gm, lm;

    final List<BigInteger> p2, p3;

    public Blocks(int gm, int lm) {
        this.gm = gm;
        this.lm = lm;

        p2 = IntStream.range(0, lm).mapToObj(TWO::pow).toList();
        p3 = IntStream.range(0, lm).mapToObj(THREE::pow).toList();
    }

    void dump() {
        dump("r", this::r);
        dump("l", this::l);
        dump("ur", this::ur);
    }

    public static void main(String ... args) {
        new Blocks(32, 32).dump();
    }

    void dump(String name, IntBinaryOperator op) {
        for (int g = -2; g < gm; ++g) {
            if(g<-1)
                System.out.format("%-3s|", name);
            else if(g<0)
                System.out.print("---|");
            else
                System.out.format("%3d|", g);

            for (int l = 0; l < lm; ++l) {
                if(g==-1)
                    System.out.print("----");
                else {
                    int k = g < 0 ? l : op.applyAsInt(g, l);
                    System.out.format("%4d", k);
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    int ur(int g, int l) {
        long g2 = (2L*g+1);
        if(l<32)
            return (int)((g2 * (1L<<l) - 1) % 3);
        else
            return p2.get(l).multiply(BigInteger.valueOf(g2))
                    .mod(THREE).intValue();
    }

    BigInteger top(int g, int l) {
        long g2 = (2L*g+1);
        return p3.get(l).multiply(BigInteger.valueOf(g2))
                .subtract(ONE);
    }

    int r(int g, int l) {
        BigInteger top = top(g, l);
        int n = top.getLowestSetBit();
        return n;
    }

    int l(int g, int l) {
        BigInteger top = top(g, l);
        return top.shiftRight(top.getLowestSetBit())
                .add(ONE).getLowestSetBit();
    }
}
