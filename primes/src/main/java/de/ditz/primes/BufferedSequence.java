package de.ditz.primes;

import java.nio.ByteBuffer;
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

    final long offset;

    public BufferedSequence(long offset, ByteBuffer buffer) {
        this.buffer = buffer;
        this.offset = offset;
    }

    public BufferedSequence(long offset, int size) {
        this.buffer = ByteBuffer.allocateDirect(size);
        this.offset = offset;
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

    public long limit() {
        return offset + ByteSequence.SIZE * buffer.capacity();
    }

    public long count() {
        return stream().mapToInt(ByteSequence::size).sum();
    }

    @Override
    public <R> R process(long start, LongFunction<? extends R> process) {

        if(start>=limit())
            return null;

        if(start<offset)
            start=offset;

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
    public boolean drop(long number) {

        number -= offset;

        if(number>0) {
            long pos = ByteSequence.count(number);
            if (pos < buffer.capacity()) {
                int seq = 0xff&buffer.get((int) pos);
                long rem = number % ByteSequence.SIZE;
                int dropped = ByteSequence.expunge(seq, rem);

                System.out.format("%5d = %3d + %3d %02x -> %02x %s\n",
                        number, pos, rem, seq, dropped, dropped == seq ? "!" : "");

                if (dropped != seq) {
                    buffer.put((int) pos, (byte)dropped);
                    return true;
                } else
                    return false;
            }
        }

        return false;
    }

    BufferedSequence sieve(Sequence primes, long start) {
        return sieve(primes, start, this.offset);
    }

    BufferedSequence sieve(Sequence primes, long start, long offset) {

        long limit = limit();

        BufferedSequence result = primes.process(start, p0 -> {
            long skip = 1 + offset / p0;
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

    public static BufferedSequence build(long until) {

        return Sequences.ROOT.process(7, new LongFunction<>() {

            BufferedSequence current;

            {
                current = new BufferedSequence(0, 1);
                current.buffer.put(0, Sequences.ROOT.getByte());
            }

            @Override
            public BufferedSequence apply(long factor) {

                if(factor>until)
                    return current;

                // compose a new buffer of factor * capacity of current buffer
                int cap = current.buffer.capacity();
                long product = factor * cap;

                if(product>Integer.MAX_VALUE)
                    throw new IndexOutOfBoundsException();

                ByteBuffer buffer = ByteBuffer.allocateDirect((int) product);
                for (int i = 0; i < factor; ++i) {
                    buffer.put(i * cap, current.buffer, 0, cap);
                }

                current = new BufferedSequence(0, buffer).sieve(current, factor, cap*ByteSequence.SIZE);

                if(factor < until)
                    return null;
                else
                    return current;
            }
        });
    }

    public static void main(String ... args) {
        BufferedSequence sequence = build(11);

        System.out.format("%,d %,d\n", sequence.limit(), sequence.count());

        //sequence.process(Sequence.all(System.out::println));
    }
}
