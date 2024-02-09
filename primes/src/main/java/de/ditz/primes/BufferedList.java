package de.ditz.primes;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.02.24
 * Time: 00:51
 */
public class BufferedList implements Sequence {

    final List<BufferedSequence> buffers;

    public BufferedList(List<BufferedSequence> buffers) {
        this.buffers = buffers;
    }

    public int size() {
        return buffers.size();
    }

    public BufferedSequence get(int index) {
        return buffers.get(index);
    }

    public long limit() {
        return buffers.isEmpty() ? 0 : buffers.getLast().limit();
    }

    public long count() {
        return count(limit());
    }

    public long count(long limit) {
        long count = 0;

        for (BufferedSequence buffer : buffers) {
            if (buffer.offset() >= limit)
                break;
            count += buffer.count(limit);
        }
        return count;
    }

    protected int blockSize() {
        return buffers.isEmpty() ? 0 : buffers.getFirst().capacity();
    }

    /**
     * Emit primes to a target processor.
     * The root block misses primes below 17.
     *
     * @param start first prime to emit.
     * @param target to preocess primes.
     * @return a target result or null if exceeded.
     */
    @Override
    public <R> R process(long start, Target<? extends R> target) {

        if(start>limit())
            return null;

        final long block = blockSize()*ByteSequence.SIZE;

        // find blocks to skip.
        int n = (int)(start/block);

        // first partial block
        R result = null;
        if(start>n*block) {
            BufferedSequence sequence = buffers.get(n++);
            result = sequence.process(start, target);
        }

        while(result == null && n<buffers.size()) {
            BufferedSequence sequence = buffers.get(n++);
            result = sequence.process(target);
        }

        return result;
    }


    public long[] stat(long[] stat) {

        for (BufferedSequence buffer : buffers) {
            buffer.stat(stat);
        }

        return stat;
    }
}
