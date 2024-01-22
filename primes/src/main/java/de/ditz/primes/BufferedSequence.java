package de.ditz.primes;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.AbstractList;
import java.util.RandomAccess;
import java.util.function.LongFunction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.01.24
 * Time: 14:43
 */
public class BufferedSequence extends AbstractList<ByteSequence> implements RandomAccess, Sequence {

    final ByteBuffer buffer;

    // byte offset.
    final long base;

    public BufferedSequence(long base, ByteBuffer buffer) {
        this.buffer = buffer;
        this.base = base;
    }

    public BufferedSequence(long offset, int size) {
        this.buffer = ByteBuffer.allocateDirect(size);
        this.base = offset;
    }


    public BufferedSequence init() {
        LongBuffer lb = buffer.asLongBuffer();
        int size = buffer.capacity();
        for(int i=0; i<size/8; ++i)
            lb.put(-1L);

        for(int i=8*lb.capacity(); i<size; ++i)
            buffer.put(i, (byte)0xff);

       return this;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public ByteSequence get(int i) {
        return Sequences.sequence(0xff&buffer.get(i));
    }

    @Override
    public int size() {
        return buffer.capacity();
    }

    public long offset() {
        return ByteSequence.SIZE * base;
    }

    public long limit() {
        return ByteSequence.SIZE * (base + buffer.capacity());
    }

    public long count() {
        return stream().mapToInt(ByteSequence::size).sum();
    }

    @Override
    public <R> R process(long start, LongFunction<? extends R> process) {

        if(start>=limit())
            return null;

        long offset = offset();

        if(start/ByteSequence.SIZE<offset)
            start=offset*ByteSequence.SIZE;

        R result = null;

        // find bytes to skip.
        int n = (int)((start-offset)/ByteSequence.SIZE);

        // process first block
        start -= n*ByteSequence.SIZE;
        if(start>0 && n<size()) {
            int m = 0xff & buffer.get(n);
            ByteSequence sequence = Sequences.sequence(m);
            result = sequence.process(start, process, offset + n*ByteSequence.SIZE);
            ++n;
        }

        // continue with remaining bytes
        while(result == null && n<size()) {
            int m = 0xff & buffer.get(n);
            ByteSequence sequence = Sequences.sequence(m);
            result = sequence.process(process, offset + n*ByteSequence.SIZE);
            ++n;
        }

        return result;
    }

    /**
     * Drop some number from the sequence.
     * @param number to drop.
     * @return true if the number was dropped.
     */
    public boolean drop(final long number) {

        long pos = ByteSequence.count(number) - base;

        if(pos>=0 && pos<buffer.capacity()) {
            int seq = 0xff&buffer.get((int) pos);
            long rem = number % ByteSequence.SIZE;
            int dropped = ByteSequence.expunge(seq, rem);

            // System.out.format("%5d = %2d+%d @ %2d  %02x -> %02x %s\n",
            // number, base, pos, rem, seq, dropped, dropped == seq ? "!" : "");

            if (dropped != seq) {
                buffer.put((int) pos, (byte)dropped);
                return true;
            }

        }

        return false;
    }

    /**
     * Slice current buffer.
     * @param start offset in bytes.
     * @param size in bytes.
     * @return a sliced buffer.
     */
    public BufferedSequence slice(int start, int size) {
        if(start < base)
            throw new IllegalArgumentException();

        start -= base;
        ByteBuffer slice = buffer.slice(start, size);
        return new BufferedSequence(base+start, slice);
    }

    BufferedSequence sieve(Sequence primes, long start) {

        long limit = limit();

        BufferedSequence result = primes.process(start, p0 -> {
            long skip = (offset() + p0-1) / p0;
            if (skip < p0)
                skip = p0;

            if (skip * p0 >= limit)
                return this;

            primes.process(skip, p1 -> {
                long product = p0 * p1;
                if (product > limit)
                    return this;

                boolean dropped = drop(product);
                if (dropped)
                    return null;
                else
                    return null;
            });
            return null;
        });

        if(result==null)
            throw new IllegalStateException("primes under run");

        return result;
    }
}
