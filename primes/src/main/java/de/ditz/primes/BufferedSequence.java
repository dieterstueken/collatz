package de.ditz.primes;

import java.nio.ByteBuffer;
import java.util.function.LongPredicate;

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
    public boolean forEach(long start, LongPredicate until, long base) {

        int i = start <base ? 0 : (int)((base - start)/CompactSequence.SIZE);

        while(i<buffer.capacity()) {
            byte m = buffer.get(i);
            CompactSequence s = CompactSequence.sequence(m);
            if(s.forEach(start, until, base+(long)CompactSequence.SIZE*i))
                return true;
            ++i;
        }

        return false;
    }
}
