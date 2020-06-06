package de.ditz.primes;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  01.06.2020 13:12
 * modified by: $
 * modified on: $
 */
public class Primes {

    static final int BLOCK = Short.MAX_VALUE;
    static final int SIEVE = 64*Short.MAX_VALUE;

    final ArrayList<Block> blocks = new ArrayList<>();
    long size = 0;
    long last = 1;

    public Primes() {
        add(2L);
        add(3L);
        add(5L);
        add(7L);
        add(11L);
        add(13L);
        add(17L);
        add(19L);
    }

    private synchronized long size() {
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

    private long compute(BooleanSupplier stop) {

        int n=0;
        int count = 0;

        if(stop.getAsBoolean())
            return count;

        Sieve sieve = new Sieve((last()+1) | 1L, stop);
        sieve.fork();

        while(sieve!=null) {
            Sieve next = sieve.join();
            count += sieve.finish();
            sieve = next;
            ++n;
        }

        return count;
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

        public double density() {
            return (p[BLOCK-1] - p[0]) * 1.0 / BLOCK;
        }
    }

    final AtomicInteger ready = new AtomicInteger();

    final AtomicReference<Sieve> pending = new AtomicReference<>();

    class Sieve extends RecursiveTask<Sieve> {

        final BitSet sieve = new BitSet(SIEVE);

        final long base;

        long processed = 0;

        final BooleanSupplier stop;

        Sieve(long base, BooleanSupplier stop) {
            this.base = base; //(last()+1) | 1L;
            this.stop = stop;
        }

        void fill() {

            for(long limit = size(); processed<limit; limit = size()) {
                for (long i = processed; i < limit; i++) {
                    long p = Primes.this.get(i);
                    if(!sieve(p))
                        return;
                }
            }
        }

        private Sieve next() {
            if(stop.getAsBoolean())
                return null;

            Sieve next = new Sieve(base + SIEVE, stop);

            int n = ready.get();
            if(n<128)
                next.fork();
            else {
                if (pending.getAndSet(next) != null)
                    throw new IllegalStateException("multiple pendings");
            }
            
            return next;
        }

        @Override
        protected Sieve compute() {
            Sieve next = next();

            fill();

            ready.incrementAndGet();

            return next;
        }

        /**
         * Finisch sieving.
         * @return number of primes added.
         */
        int finish() {

            // fill any remaining.
            fill();

            // process new primes
            int count=0;
            for(int i=sieve.nextClearBit(0); i>=0 && i<SIEVE; i = sieve.nextClearBit(i+1)) {
                long prime = base + 2*i;
                add(prime);
                sieve(prime);
                ++count;
            }

            // mark already done
            last = base + 2*SIEVE - 2;

            int n = ready.decrementAndGet();
            if(n<32) {
                Sieve next = pending.getAndSet(null);
                if (next != null)
                    next.fork();
            }

            return count;
        }

        private long i0(long prime) {
            long p2 = 2*prime;
            long k = (base - prime + p2-1)/p2;
            return (p2*k + prime - base)/2;
        }

        private boolean sieve(long prime) {

            // terminate
            if(prime>(base+SIEVE)/prime)
                return false;

            // odd numbers only
            if(prime>2) {
                for (long index = i0(prime); index < SIEVE; index += prime) {
                    sieve.set((int) index);
                }
            }

            ++processed;

            return true;
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

        primes.compute(() -> primes.last()>4L*Integer.MAX_VALUE);
        //primes.compute(() -> primes.blocks.size()>80);

        int size = primes.blocks.size();
        Block block = primes.blocks.get(size -2);
        System.out.format("blocks: %d, range: %d, density: %.1f\n", size, block.get(block.size()-1) - block.get(0), block.density());

        System.out.format("%,13d %,13d\n", primes.size(), primes.last());
    }
}
