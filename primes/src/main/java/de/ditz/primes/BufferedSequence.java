package de.ditz.primes;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.LongFunction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.01.24
 * Time: 14:43
 */
public class BufferedSequence extends AbstractList<ByteSequence> implements RandomAccess, Sequence {

    protected final ByteBuffer buffer;

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
            byte m = buffer.get((int)i);
            ByteSequence s = Sequences.sequence(m);
            result = s.process(start, process, base+(long) ByteSequence.SIZE*i);
        }

        return result;
    }

    public boolean drop(long index) {

        if(index>0) {
            long pos = ByteSequence.count(index);
            if (pos < buffer.capacity()) {
                byte seq = buffer.get((int) pos);
                int dropped = ByteSequence.expunge(seq, (int)(index % ByteSequence.SIZE));
                if (dropped != seq) {
                    buffer.put((int) pos, (byte)dropped);
                    return true;
                }
            }
        }

        return false;
    }

    public static BufferedSequence build(long limit) {

        return Sequences.ROOT.process(7, new LongFunction<BufferedSequence>() {

            BufferedSequence current;

            {
                current = new BufferedSequence(1);
                current.buffer.put(0, Sequences.ROOT.getByte());
            }

            @Override
            public BufferedSequence apply(long factor) {

                if(factor>limit)
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

                current = new Sieve(next, factor).sieve(next, 0);

                if(factor < limit)
                    return null;
                else
                    return current;
            }
        });
    }

    public static void main(String ... args) {
        BufferedSequence sequence = build(7);

        System.out.format("%,d %,d\n", sequence.limit(), sequence.count());

        sequence.process(Sequence.all(System.out::println));
    }
}
