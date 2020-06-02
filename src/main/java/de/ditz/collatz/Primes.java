package de.ditz.collatz;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  01.06.2020 13:12
 * modified by: $
 * modified on: $
 */
public class Primes {

    static final int BLOCK = Short.MAX_VALUE;
    static final int SIEVE = 4096;

    final List<Block> blocks = new ArrayList<>();
    long size = 0;
    long last = 1;

    public Primes() {
        add(2L);
    }

    private long size() {
        return size;
    }

    private long last() {
        return last;
    }

    private Block block(long index) {
        long i = index/BLOCK;

        if(i<blocks.size())
            return blocks.get((int) i);

        // extend last block on demand
        if(index != size)
            throw new IndexOutOfBoundsException("block overflow");

        return new Block();
    }

    public long get(long index) {

        if(index<size)
            return block(index).getPrime(index);

        throw new IndexOutOfBoundsException("overflow");
    }

    protected void add(long prime) {
        Block block = block(size);
        block.setPrime(size, prime);
        last = prime;
        ++size;
    }

    private int grow() {
        return new Sieve().run();
    }

    public void forEach(LongBinaryOperator sieve) {

        for (long i = 0; i < size; i++) {
            long p = get(i);
            sieve.applyAsLong(i, p);
        }
    }

    public void forEach(LongConsumer sieve) {

        for (long i = 0; i < size; i++) {
            long p = get(i);
            sieve.accept(p);
        }
    }

    class Block extends AbstractList<Long> {

        final int index;
        final long base;
        final int[] p = new int[BLOCK];

        Block() {
            this.index = blocks.size();
            this.base = last();
            blocks.add(this);
        }

        @Override
        public Long get(int i) {
            int value = p[i];
            return value==0 ? -1 : base + value;
        }

        @Override
        public int size() {
            long tail = Primes.this.size() - this.index*BLOCK;
            return Math.min(BLOCK, (int)tail);
        }

        long getPrime(long i) {
            long offset = i - BLOCK*index;
            return get((int)offset);
        }

        void setPrime(long i, long prime) {
            long offset = i - BLOCK*index;
            long value = prime-base;

            if(value>Integer.MAX_VALUE)
                throw new IllegalArgumentException("block overflow");

            p[(int)offset] = (int)value;
        }
    }

    class Sieve {
        final long base;

        final BitSet sieve = new BitSet(SIEVE);

        Sieve() {
            base = (last()+1) | 1L;
        }

        public int run() {
            // process known primes
            Primes.this.forEach(this::sieve);

            // process new primes
            int count = 0;
            for(int i=sieve.nextClearBit(0); i>=0 && i<SIEVE; i = sieve.nextClearBit(i+1)) {
                long prime = base + 2*i;
                add(prime);
                sieve(prime);
                ++count;
            }

            // mark already done
            last = base + 2*SIEVE - 2;
            return count;
        }

        private long i0(long prime) {
            long p2 = 2*prime;
            long k = (base - prime + p2-1)/p2;
            return (p2*k + prime - base)/2;
        }

        void sieve(long prime) {
            // odd numbers only
            if(prime<=2)
                return;

            for(long index = i0(prime); index<SIEVE; index += prime) {
                sieve.set((int) index);
            }
        }
    }

    public static void main(String ... args) {
        Primes primes = new Primes() {

            long previous = 0;
            long gap = 0;

            @Override
            protected void add(long prime) {
                super.add(prime);
                long g = prime-previous;

                if(g>gap) {
                    System.out.format("gap %,13d %,5d %,13d\n", size, g, prime);
                    gap = g;
                }

                previous = prime;
            }
        };

        //long last = 0;

        for(int i=0; i<Short.MAX_VALUE; ++i) {
            long count = primes.grow();

            //long time = System.currentTimeMillis();
            //if(time>last+1000) {
            //    System.out.format("%,13d %,5d\n", primes.size(), count);
            //    last = time;
            //}

            if(primes.last()>Integer.MAX_VALUE/32)
                break;
        }

        System.out.format("%,13d %,13d\n", primes.size(), primes.last());
    }
}
