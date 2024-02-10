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
     * The root block misses primes below 17, so the first block is substituted.
     *
     * @param start first prime to emit.
     * @param target to preocess primes.
     * @return a target result or null if exceeded.
     */
    @Override
    public <R> R process(long start, Target<? extends R> target) {
        R result = null;

        // substitute root block
        if(start<ByteSequence.SIZE) {
            result = Sequences.PRIMES.process(start, target);
            if(result!=null)
                return result;

            // continue after 29
            start = 30;
        }

        if(start>limit())
            return null;

        final long block = blockSize()*ByteSequence.SIZE;

        // find blocks to skip.
        int n = (int)(start/block);

        while(result == null && n<buffers.size()) {
            BufferedSequence sequence = buffers.get(n++);
            result = sequence.process(start, target);
        }

        return result;
    }
}
