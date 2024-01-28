package de.ditz.primes;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.01.24
 * Time: 14:43
 */
public class BufferedSequence implements Sequence {

    final ByteBuffer buffer;

    // byte offset.
    final long base;

    final List<ByteSequence> sequences = new RandomList<>() {

        @Override
        public int size() {
            return buffer.capacity();
        }

        @Override
        public ByteSequence get(int i) {
            return Sequences.sequence(0xff&buffer.get(i));
        }
    };

    public BufferedSequence(long base, ByteBuffer buffer) {
        this.buffer = buffer;
        this.base = base;
    }

    public BufferedSequence(long base, int size) {
        this.buffer = ByteBuffer.allocateDirect(size);
        this.base = base;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int capacity() {
        return buffer.capacity();
    }

    public long offset() {
        return ByteSequence.SIZE * base;
    }

    public long size() {
        return ByteSequence.SIZE * capacity();
    }

    public long limit() {
        return ByteSequence.SIZE * (base + capacity());
    }

    public long count() {
        return sequences.stream().mapToInt(ByteSequence::size).sum();
    }

    @Override
    public <R> R process(final long start, Target<? extends R> target) {

        long offset = offset();

        // find bytes to skip.
        int n = (int)(Math.max(start, offset)/ByteSequence.SIZE);
        offset += n*ByteSequence.SIZE;

        R result = null;

        // process first partial block
        if(start>offset && n<buffer.capacity()) {
            int m = 0xff & buffer.get(n++);
            ByteSequence sequence = Sequences.sequence(m);
            result = sequence.process(start, target, offset);
            offset += ByteSequence.SIZE;
        }

        // continue with full remaining bytes
        while(result == null && n<buffer.capacity()) {
            int m = 0xff & buffer.get(n++);
            ByteSequence sequence = Sequences.sequence(m);
            result = sequence.process(target, offset);
            offset += ByteSequence.SIZE;
        }

        return result;
    }

    /**
     * Drop some factor from this sequence.
     * @param factor to drop.
     * @return this if the factor is beyond the limit.
     */
    public BufferedSequence drop(final long factor) {
        long pos = ByteSequence.count(factor) - base;

        if(pos>=0) {
            if(pos<capacity()) {
                int seq = 0xff & buffer.get((int) pos);
                long rem = factor % ByteSequence.SIZE;
                int dropped = CompactSequence.expunge(seq, rem);

                if (dropped != seq) {
                    buffer.put((int) pos, (byte) dropped);
                } else {
                    System.out.format("%5d = %2d * 30 + %2d %02x -> %02x %s\n",
                            factor, pos, rem, seq, dropped, dropped == seq ? "!" : "");
                    return null;
                }
            } else {
                // done
                return this;
            }
        }

        // continue
        return null;
    }

    public long[] stat(long[] stat) {
        int cap = buffer.capacity();
        int l = stat.length;
        for(int i=0; i<cap; ++i) {
            int seq = 0xff & buffer.get((int) i);
            int n = Sequences.sequence(seq).size();
            if(n<l)
                ++stat[n];
        }

        return stat;
    }

    public long[] stat() {
        return stat(new long[8]);
    }

    protected class Sieve implements Target<BufferedSequence> {

        long limit = limit();

        final Sequence primes;

        final PowerTarget<BufferedSequence> target = new PowerTarget<>(BufferedSequence.this::drop);

        protected Sieve(Sequence primes) {
            this.primes = primes;
        }

        @Override
        public BufferedSequence apply(long prime) {
            // get remaining factor to reach the offset
            long factor = offset() / prime;
            if(factor<prime)
                factor = prime;

            if (factor * prime < limit) {

                target.reset(prime);
                primes.process(factor+1, target);

                // continue with larger primes
                return null;
            } else
                return BufferedSequence.this;
        }
    }

    protected Sieve sieve(Sequence primes) {
        return new Sieve(primes);
    }

    BufferedSequence sieve(Sequence primes, long start) {


        BufferedSequence result = primes.process(start, sieve(primes));

        if(result==null)
            throw new IllegalStateException("primes under run");

        return result;
    }

    /**
     * Slice current buffer.
     * @param start position in bytes.
     * @param size in bytes.
     * @return a sliced buffer.
     */
    public BufferedSequence slice(int start, int size) {

        if(start+size > capacity())
            size = capacity() - start;

        // negative size generates 
        ByteBuffer slice = buffer.slice(start, size);

        // apply base again
        return new BufferedSequence(base+start, slice);
    }

    /**
     * Return a list of slices with a given length each.
     * The last slice may be smaller if the overall length
     * is not a multiple of the slices' length.
     *
     * @param length of each slice in bytes.
     * @return a virtual List of Buffered slices.
     */
    public List<BufferedSequence> slices(int length) {
        return new Slices(length);
    }

    protected class Slices extends RandomList<BufferedSequence> {

        final int length;

        protected Slices(int length) {
            this.length = length;
            if(length<1)
                throw new IllegalArgumentException("invalid buffer length");
        }

        @Override
        public BufferedSequence get(int index) {
            int start = index*length;
            int capacity = Math.min(length, capacity()-start);
            return slice(start, length);
        }

        @Override
        public int size() {
            return capacity() / length;
        }
    }
}
