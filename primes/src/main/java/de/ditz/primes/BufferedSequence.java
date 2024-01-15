package de.ditz.primes;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.01.24
 * Time: 14:43
 */
public class BufferedSequence {

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

    public int size() {
        return CompactSequence.SIZE * buffer.capacity();
    }

    public void reset() {
        for(int i=0; i<buffer.capacity(); ++i)
            buffer.put(i, (byte)0xff);
    }

    public boolean forEach(LongPredicate until, long base) {
        for(int i=0; i<buffer.capacity(); ++i) {
            byte m = buffer.get(i);
            CompactSequence s = CompactSequence.sequence(m);
            if(s.forEach(until, base+CompactSequence.SIZE*i))
                return true;
        }

        return false;
    }

    void sieve(long factor, long offset) {
        int size = buffer.capacity();
        for(long pos = offset % factor; pos<size; pos += factor) {
            int i = (int)(pos/30);
            byte m = buffer.get(i);
            byte n = CompactSequence.mask(m, i%30);
            if(m!=n)
                buffer.put(i, n);
        }
    }

    void sieve(long factor) {
        sieve(factor, 0);
    }

    public static final List<Integer> ROOTS;

    static final int SIZE;

    public static BufferedSequence BASE;

    static {
        ROOTS = List.of(7,11,13,19);

        SIZE = ROOTS.stream().reduce(1, (a,b)->a*b);
        BufferedSequence base = new BufferedSequence(SIZE);
        ROOTS.forEach(base::sieve);

        BASE = new BufferedSequence(base.buffer.asReadOnlyBuffer()) {
            @Override
            public void reset() {
                throw new UnsupportedOperationException();
            }

            @Override
            void sieve(long factor, long offset) {
                throw new UnsupportedOperationException();
            }
        };
    }

    static BufferedSequence create() {
        return new BufferedSequence(SIZE) {
            @Override
            public void reset() {
                buffer.put(0, BASE.buffer, 0, buffer.capacity());
            }
        };
    }
}
