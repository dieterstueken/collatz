package de.ditz.primes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.01.24
 * Time: 14:43
 */
public class BufferedSequence implements Sequence, LimitedTarget<BufferedSequence> {

    public static long debug = 0;

    final ByteBuffer buffer;

    // byte offset.
    public final long base;

    int dups = 0;

    final List<ByteSequence> sequences = new RandomList<>() {

        @Override
        public int size() {
            return buffer.capacity();
        }

        @Override
        public ByteSequence get(int i) {
            return sequence(i);
        }
    };

    public List<ByteSequence> sequences() {
        return sequences;
    }

    public BufferedSequence(long base, ByteBuffer buffer) {
        this.buffer = buffer;
        this.base = base;
    }

    public BufferedSequence(long base, int capacity) {
        this.buffer = ByteBuffer.allocate(capacity);
        this.base = base;
    }

    public String toString() {
        return String.format("BufferedSequence{%d:%d:%d}", base, capacity(), offset());
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int capacity() {
        return buffer.capacity();
    }

    @Override
    public long offset() {
        return ByteSequence.SIZE * base;
    }

    public long size() {
        return ByteSequence.SIZE * capacity();
    }

    @Override
    public long limit() {
        return ByteSequence.SIZE * (base + capacity());
    }

    public long count() {
        return sequences.stream().parallel().mapToInt(ByteSequence::size).sum();
    }

    public long count(long limit) {
        if(limit>=limit())
            return count();

        limit -= offset();
        int l = (int) (limit / ByteSequence.SIZE);

        // fast track full sequences.
        long count = sequences.subList(0, l).stream().parallel().mapToInt(ByteSequence::size).sum();

        // possible last fragment
        count += sequences.get(l).count(limit%ByteSequence.SIZE);

        return count;
    }

    /**
     * Process all sequences starting at start until done.
     * @param start only pass values >= start.
     * @param target to generate a result to stop the processing.
     * @return a result from target or null of exceeded.
     * @param <R> expected type of result.
     */
    @Override
    public <R> R process(long start, Target<? extends R> target) {

        // absolute index
        long index = start / ByteSequence.SIZE - base;

        if(index<0) {
            index = 0;
            start = 0;
        }

        R result = null;
        long offset = (index + base) * ByteSequence.SIZE;

        // process possible first partial sequence
        if(start>offset) {
            ByteSequence sequence = sequence(index++);
            if(sequence==null)
                // already exceeded
                return null;

            result = sequence.process(start, target, offset);
            offset += ByteSequence.SIZE;
        }

        while(result==null) {
            ByteSequence sequence = sequence(index++);
            if(sequence==null)
                return null;

            result = sequence.process(target, offset);
            offset += ByteSequence.SIZE;
        }

        return result;
    }

    /**
     * Get the sequence at index or null of exceeded.
     * @param index of ByteSequence to get.
     * @return a ByteSequence or null if exceeded
     */
    protected ByteSequence sequence(long index) {
        if(index>=buffer.capacity())
            return null;

        int m = 0xff & buffer.get((int)index);
        return Sequences.sequence(m);
    }

    /**
     * Target apply implementation works a bit different from drop().
     *
     * @param factor to test.
     * @return null to continue and this to terminate.
     */
    @Override
    public BufferedSequence process(final long factor) {
        Boolean dropped = drop(factor);
        if(dropped == null)
            return this;    // terminate processing

        if(dropped==false)
            ++dups;

        return null; // continue processing
    }

    public int dups() {
        return dups;
    }

    /**
     * Drop some factor from this sequence.
     * @param factor to drop.
     * @return true if hit, false if missed and null if beyond the limit.
     */
    public Boolean drop(final long factor) {
        long pos = ByteSequence.indexOf(factor);

        // below offset
        if(pos<base)
            return false;

        pos -= base;

        // done
        if(pos>=capacity())
            return null;

       int seq = 0xff & buffer.get((int) pos);
       long rem = factor % ByteSequence.SIZE;
       int dropped = CompactSequence.expunge(seq, rem);
       boolean hit = dropped != seq;

       if(hit) {
           buffer.put((int) pos, (byte) dropped);
       }
       
       if(debug!=0 && (!hit && debug<0 || factor==debug || -factor == debug)) {
           System.out.format("%5d = %2d * 30 + %2d %02x -> %02x %s\n",
                    factor, pos, rem, seq, dropped, hit ? "" : "!");
           return hit;
       }

       // continue
        return hit;
    }

    public long[] stat(long[] stat) {
        int cap = buffer.capacity();
        int l = stat.length;
        int last = 0;

        for(int i=0; i<cap; ++i) {
            int seq = 0xff & buffer.get((int) i);

            int n = Sequences.sequence(seq).size();

            if(n<l)
                ++stat[n];

            if(l>8)
                stat[8] += n;

            if(l>9) {
                int k=0;
                last <<= 8;
                last |= seq;
                if((last&0x101) == 0x101)
                    ++k;

                if((last&12) == 12)
                    ++k;

                if((last&0x30) == 0x30)
                    ++k;

                if(k!=0)
                    stat[9] += k;
            }
        }

        return stat;
    }

    public long[] stat() {
        return stat(new long[8]);
    }

    /**
     * Slice current buffer.
     * @param start position in bytes.
     * @param size in bytes.
     * @return a sliced buffer.
     */
    public BufferedSequence slice(int start, int size) {

        if(start+size > capacity())
            size = capacity() - start;

        // negative size generates 
        ByteBuffer slice = buffer.slice(start, size);

        // apply base again
        return new BufferedSequence(base+start, slice);
    }

    /**
     * Return a list of slices with a given length each.
     * The last slice may be smaller if the overall length
     * is not a multiple of the slices' length.
     *
     * @param length of each slice in bytes.
     * @return a virtual List of Buffered slices.
     */
    public List<BufferedSequence> slices(int length) {
        return new Slices(length);
    }

    protected class Slices extends RandomList<BufferedSequence> {

        final int length;

        protected Slices(int length) {
            this.length = length;
            if(length<1)
                throw new IllegalArgumentException("invalid buffer length");
        }

        @Override
        public BufferedSequence get(int index) {
            int start = index*length;
            int capacity = Math.min(length, capacity()-start);
            return slice(start, length);
        }

        @Override
        public int size() {
            return capacity() / length;
        }
    }
}
