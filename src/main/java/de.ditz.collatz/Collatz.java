package de.ditz.collatz;

import java.math.BigInteger;
import java.util.AbstractList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.04.20
 * Time: 17:43
 */
public class Collatz {

    static final BigInteger THREE = BigInteger.valueOf(3L);

    public static final Collatz ROOT = new Collatz();

    // k = 3*index + m;
    // n = 2*k + 1;
    final BigInteger j;
    final int m;

    final BigInteger k;

    final int step;

    final int len;

    final Collatz succ;

    transient Collatz pred;

    final List<Collatz> seq;

    private Collatz() {
        this.j = BigInteger.ZERO;
        this.m = 0;
        this.k = BigInteger.ZERO;
        this.step = 1;
        this.succ = this;
        this.pred = this;
        this.len = 0;
        this.seq = List.of();
    }

    private Collatz(Collatz succ, int step, BigInteger j, int m) {
        this.succ = succ;
        this.step = step;
        this.j = j;
        this.m = m;
        this.k = j.multiply(THREE).add(BigInteger.valueOf(m));
        this.len = succ.len+1;

        this.seq = new AbstractList<>() {

            @Override
            public Collatz get(int index) {
                Collatz c = Collatz.this;

                for(int i=index+1; i<len; ++i)
                    c = c.succ;

                return c;
            }

            @Override
            public int size() {
                return len;
            }
        };
    }

    public Collatz succ() {
        return succ;
    }

    public Collatz pred(int i) {
        if(i<0)
            throw new IllegalArgumentException();

        if(i==0)
            return pred();
        
        Collatz pi = pred(i-1);
        if(pi==null)
            pi = pred(i-1);
        return pi.sibl();
    }

    public Collatz pred() {

        if(m==1)
            return null;

        Collatz pred = this.pred;
        if(pred!=null)
            return pred;

        synchronized (this) {
            pred = this.pred;
            if(pred!=null)
                return pred;

            boolean even = m!=0;

            BigInteger k = even
                    ? j.shiftLeft(1).setBit(0) // 2j+1
                    : j.shiftLeft(2);          // 4j

            BigInteger[] m = k.divideAndRemainder(THREE);

            this.pred = pred = new Collatz(this, even ? 0 : 1, m[0], m[1].intValueExact());
            return pred;
        }
    }

    transient Collatz sibl;

    public Collatz sibl() {
        Collatz sibl = this.sibl;
        if(sibl!=null)
            return sibl;

        synchronized(this) {
            sibl = this.sibl;
            if(sibl!=null)
                return sibl;

            // 3kl = 2^l(2k+1)-2
            // 3kl+2 = 4 2^l(2k+1)-2
            //       = 4(2^l(2k+1)-2)+8-2
            //       = 4 3kl + 6
            // k' = 4k+2 = 4(3j+m)+2
            // 3j' + m' =  12j + 4m + 2
            //          =  3(4j + m) + m + 2
            // j' = 4j + (4m+2)/3
            // m' = (4m+2)%3
            //
            // m m' j'
            // 0 2 4j
            // 1 0 4j + 2
            // 2 1 4j + 3

            int mm = (4 * m + 2) % 3;
            BigInteger jj = j.shiftLeft(2);

            if (m > 0)
                jj = jj.setBit(1);

            if (m > 1)
                jj = jj.setBit(0);

            this.sibl = sibl = new Collatz(succ, step + 2, jj, mm);
        }
        return sibl;
    }

    @Override
    public String toString() {
        return String.format("3*%d+%d=%d[%d]", j, m, k, step);
    }

    public static Collatz get(BigInteger k) {
        if(k.signum()==0)
            return ROOT;

        if(k.signum()<0)
            throw new IllegalArgumentException();

        BigInteger k3 = k.add(k.add(BigInteger.ONE).shiftLeft(1));
        int L = k3.getLowestSetBit();
        BigInteger ks = k3.shiftRight(L+1);
        
        Collatz succ = get(ks);
        Collatz result = succ.pred(L / 2);
        return result;
    }

    public static Collatz get(long k) {
        return get(BigInteger.valueOf(k));
    }


    public static void main(String ... args) {
        Collatz c = Collatz.get(13);
        c.seq.forEach(System.out::println);
    }

}
