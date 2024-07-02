package de.ditz.collatz;

import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.04.20
 * Time: 17:43
 */

/**
 * Class Collatz represents an odd entry with n = 2k + 1.
 * Each entry has a known successor towards k=0 (n=1) which is some steps away.
 * Each entry may have a predecessor.
 *
 * Entries with k%3==1 have no predecessor.
 * Entries with k%3==0 have even predecessors (0, 2, 4, ...)
 * Entries with k%3==2 have odd predecessors (1, 3, 5, ...)
 *
 *  * Each entry has a sibling which is steps+1 from its successor away.
 */
abstract class Collatz {

    static final Comparator<Collatz> CMP = Comparator.comparing(Collatz::j).thenComparingInt(Collatz::m);

    public static Collatz max(Collatz a, Collatz b) {
        return CMP.compare(a, b) > 0 ? a : b;
    }

    static final BigInteger THREE = BigInteger.valueOf(3L);

    // k = 3*j + m;
    // n = 2*k + 1;
    final BigInteger j;
    //final int m;

    // index of this collatz
    //final BigInteger k;

    final int step;

    final int len;

    final Collatz succ;

    transient Collatz pred;

    final List<Collatz> seq;

    protected Collatz(Collatz succ, int step, BigInteger j) {
        this.succ = succ;
        this.len = succ.len+1;
        this.step = step;
        this.j = j;
        //this.m = m;
        //this.k = j.multiply(THREE).add(BigInteger.valueOf(m));

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

    protected Collatz() {
        this.j = BigInteger.ZERO;
        this.step = 1;
        this.succ = this;
        this.pred = this;
        this.len = 0;
        this.seq = List.of(this);
    }

    @Override
    public String toString() {
        return String.format("3*%d+%d=%d[%d]", j, m(), k(), step);
    }

    abstract public int m();

    public BigInteger j() {
        return j;
    }

    public BigInteger k() {
        return j.multiply(THREE).add(BigInteger.valueOf(m()));
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
        return pi.sibl();
    }

    public Collatz pred() {
        return null;
    }

    transient Collatz sibl;

    public Collatz sibl() {
        Collatz sibl = this.sibl;

        if (sibl != null)
            return sibl;
        else
            return _sibl();
    }

    abstract protected Collatz _sibl();
}
