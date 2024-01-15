package de.ditz.primes;

import java.nio.ByteBuffer;
import java.util.function.LongFunction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.01.24
 * Time: 14:43
 */
public class BufferedSequence implements Sequence {

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

    @Override
    public <R> R forEach(long start, LongFunction<? extends R> process, long base) {

        R result = null;

        for(long i = CompactSequence.count(base - start); result==null && i<buffer.capacity(); ++i) {
            byte m = buffer.get((int)i);
            CompactSequence s = CompactSequence.sequence(m);
            result = s.forEach(start, process, base+(long)CompactSequence.SIZE*i);
        }

        return result;
    }
}
