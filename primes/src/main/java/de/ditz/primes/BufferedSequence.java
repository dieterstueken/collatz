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
public class BufferedSequence extends AbstractList<CompactSequence> implements RandomAccess, Sequence {

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
    public CompactSequence get(int i) {
        return CompactSequence.sequence(0xff&buffer.get(i));
    }

    @Override
    public int size() {
        return buffer.capacity();
    }

    public long limit() {
        return (long)CompactSequence.SIZE * buffer.capacity();
    }

    public long count() {
        return stream().mapToInt(CompactSequence::size).sum();
    }

    @Override
    public <R> R process(long start, LongFunction<? extends R> process, long base) {

        R result = null;

        for(long i = CompactSequence.count(base - start); result==null && i<buffer.capacity(); ++i) {
            byte m = buffer.get((int)i);
            CompactSequence s = CompactSequence.sequence(m);
            result = s.process(start, process, base+(long)CompactSequence.SIZE*i);
        }

        return result;
    }

    public boolean drop(long index) {

        if(index>0) {
            long pos = CompactSequence.count(index);
            if (pos < buffer.capacity()) {
                byte seq = buffer.get((int) pos);
                byte dropped = CompactSequence.mask(seq, (int)(index % CompactSequence.SIZE));
                if (dropped != seq) {
                    buffer.put((int) pos, dropped);
                    return true;
                }
            }
        }

        return false;
    }

    public static BufferedSequence build(long limit) {

        return CompactSequence.root().process(7, new LongFunction<BufferedSequence>() {

            BufferedSequence current;

            {
                current = new BufferedSequence(1);
                current.buffer.put(0, CompactSequence.root().mask());
            }

            long dups = 0;

            /**
             * Drop all numbers of product*factor^n < limit from current sequence
             * @param base factor
             * @param factor additional factor
             */
            BufferedSequence drop(long base, long factor) {

                long product = base * factor;

                // done
                if(product>=current.limit())
                    return current;

                if(current.drop(product)) {
                    if(product * factor<current.limit()) {
                        drop(product, factor);
                        current.process(factor + 1, p -> drop(product, p));
                        return current;
                    }
                } else {
                    ++dups;
                }

                return null;
            }

            @Override
            public BufferedSequence apply(long factor) {

                if(factor>limit)
                    return current;

                // compose a new buffer of factor * capacity of current buffer
                int cap = current.buffer.capacity();
                long next = factor * cap;

                if(next>Integer.MAX_VALUE)
                    throw new IndexOutOfBoundsException();

                ByteBuffer buffer = ByteBuffer.allocateDirect((int) next);
                for (int i = 0; i < factor; ++i) {
                    buffer.put(i * cap, current.buffer, 0, cap);
                }

                // replace by expanded sequence
                current = new BufferedSequence(buffer);

                drop(1, factor);
                
                return factor < limit ? null : current;
            }
        });
    }

    public static void main(String ... args) {
        BufferedSequence sequence = build(7);

        System.out.format("%,d %,d\n", sequence.limit(), sequence.count());

        sequence.process(Sequence.all(System.out::println));
    }
}
