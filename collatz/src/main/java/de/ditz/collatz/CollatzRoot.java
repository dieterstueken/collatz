package de.ditz.collatz;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.06.24
 * Time: 16:00
 */
public class CollatzRoot {

    class C1 extends Collatz {

        C1(Collatz succ, int step, BigInteger j) {
            super(succ, step, j);
        }

        C1() {}

        @Override
        public int m() {
            return 1;
        };

        @Override
        protected synchronized Collatz _sibl() {
            sibl = this.sibl;
            if(sibl!=null)
                return sibl;

            BigInteger jj = j.shiftLeft(2);
            jj = jj.setBit(1);

            this.sibl = sibl = newCollatz(succ,step + 2, jj, 0);

            return sibl;
        }


    }

    class C0 extends C1 {
        C0() {}

        C0(Collatz succ, int step, BigInteger j) {
            super(succ, step, j);
        }

        @Override
        public int m() {
            return 0;
        }

        @Override
        protected synchronized Collatz _sibl() {
            sibl = this.sibl;
            if(sibl!=null)
                return sibl;

            BigInteger jj = j.shiftLeft(2);

            this.sibl = sibl = newCollatz(succ, step + 2, jj, 2);

            return sibl;
        }

        transient Collatz pred;

        @Override
        public Collatz pred() {
            Collatz pred = this.pred;

            if (pred != null)
                return pred;
            else
                return _pred();
        }

        synchronized Collatz _pred() {

            pred = this.pred;
            if(pred==null) {
                BigInteger k = j.shiftLeft(2);
                BigInteger[] m = k.divideAndRemainder(THREE);
                this.pred = pred = newCollatz(this,1, m[0], m[1].intValueExact());
            }

            return pred;
        }
    }

    class C2 extends C0 {

        C2(Collatz succ, int step, BigInteger j) {
            super(succ, step, j);
        }

        public int m() {
            return 2;
        };

        @Override
        protected synchronized Collatz _sibl() {
            sibl = this.sibl;
            if(sibl!=null)
                return sibl;

            BigInteger jj = j.shiftLeft(2).add(BigInteger.valueOf(3));

            this.sibl = sibl = newCollatz(succ, step + 2, jj, 1);

            return sibl;
        }

        @Override
        synchronized Collatz _pred() {

            pred = this.pred;
            if(pred!=null)
                return pred;

            BigInteger k = j.shiftLeft(1).setBit(0);
            BigInteger[] m = k.divideAndRemainder(THREE);
            return this.pred = pred = newCollatz(this,0, m[0], m[1].intValueExact());
        }
    }

    Collatz newCollatz(Collatz succ, int step, BigInteger j, int m) {
        Collatz c = switch(m) {
            case 0 -> new C0(succ, step, j);
            case 2 -> new C2(succ, step, j);
            default -> new C1(succ, step, j);
        };

        count.incrementAndGet();
        max.accumulateAndGet(c, Collatz::max);
        return c;
    }

    static Collatz maxl(Collatz a, Collatz b) {
        return a.len>b.len ? a : b;
    }

    final C1 root = new C0();
    final AtomicInteger count = new AtomicInteger();
    final AtomicReference<Collatz> max = new AtomicReference<>(root);

    public Collatz get(BigInteger k) {
        if(k.signum()==0)
            return root;

        if(k.signum()<0)
            throw new IllegalArgumentException();

        // successor k3 = 3*k+2 / 2^l
        BigInteger k3 = k.add(k.add(BigInteger.ONE).shiftLeft(1));
        int L = k3.getLowestSetBit();
        BigInteger ks = k3.shiftRight(L+1);

        Collatz succ = get(ks);

        Collatz result = succ.pred(L / 2);

        if(result==null) {
            result = succ.pred(L / 2);
            throw new IllegalStateException();
        }

        return result;
    }

    public Collatz get(long k) {
        return get(BigInteger.valueOf(k));
    }


    CollatzRoot show(long k) {

        Collatz c = get(k);
        System.out.print("len: ");
        System.out.println(c.len);
        c.seq.forEach(System.out::println);
        return this;
    }

    CollatzRoot max() {
        System.out.print("max: ");
        System.out.println(max.get().toString());
        return this;
    }

    CollatzRoot loop(long k) {
        for(int i=0; i<k; ++i) {
            get(i);
        }

        return this;
    }

    public static void main(String ... args) {
        new CollatzRoot().show(Long.MAX_VALUE).max();
    }
}
