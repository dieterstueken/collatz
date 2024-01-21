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

    public BufferedSequence(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public BufferedSequence(int size) {
        this.buffer = ByteBuffer.allocateDirect(size);
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
        return (long) ByteSequence.SIZE * buffer.capacity();
    }

    public long count() {
        return stream().mapToInt(ByteSequence::size).sum();
    }

    @Override
    public <R> R process(long start, LongFunction<? extends R> process, long base) {

        R result = null;

        for(long i = ByteSequence.count(base - start); result==null && i<buffer.capacity(); ++i) {
            int m = 0xff & buffer.get((int)i);
            ByteSequence sequence = Sequences.sequence(m);
            result = sequence.process(start, process, base + i*ByteSequence.SIZE);
        }

        return result;
    }

    /**
     * Drop some number from the sequence.
     * @param number to drop.
     * @return true if the number was dropped.
     */
    public boolean drop(long number) {

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

    public static BufferedSequence build(long until) {

        return Sequences.ROOT.process(7, new LongFunction<BufferedSequence>() {

            BufferedSequence current;

            {
                current = new BufferedSequence(1);
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

                BufferedSequence next = new BufferedSequence(buffer);

                current = new Sieve(next, factor, cap*ByteSequence.SIZE).sieve(next, 0);

                if(factor < until)
                    return null;
                else
                    return current;
            }
        });
    }

    public static void main(String ... args) {
        BufferedSequence sequence = build(13);

        System.out.format("%,d %,d\n", sequence.limit(), sequence.count());

        sequence.process(Sequence.all(System.out::println));
    }
}
