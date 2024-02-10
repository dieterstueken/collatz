package de.ditz.primes;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.01.24
 * Time: 12:42
 */

/*
 * Class CompactSequence contains a block of up to 8 numbers below 30=2*3*5
 * which are not a multiple of 2, 3 or 5, but including 1.
 *
 * A mask of 8 bits represents which numbers are included.
 * The numbers are packed into a long value together with the mask.
 * The value of 1 is not saved explicitly.
 * Instead, its existence is derived from the mask itself.
 *
 * All 256 instances of a CompactSequence are cached in a precalculated List.
 */
abstract public class CompactSequence extends ByteSequence {

    public static final List<Integer> FACTORS = List.of(1,7,11,13,17,19,23,29);

    final int mask;

    final long sequence;

    String asString;

    CompactSequence(int mask, long sequence) {
        this.sequence = sequence;
        this.mask = mask;
    }
    @Override
    public String toString() {
        if(asString==null)
            asString = asString();
        return asString;
    }

    protected String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%02x".formatted(mask()));
        char c='[';
        for(int i=0; i<size(); ++i) {
            sb.append(c).append(get(size()-i-1));
            c = '*';
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Drop a given factor [0,30] from a mask of factors.
     * @param mask of the compact sequence.
     * @param factor to drop.
     * @return reduced prime factor mask.
     */
    public static int expunge(int mask, long factor) {

       if(factor<7) {
          if(factor>0 && mask%2!=0) // drop 1
             mask &= 0xfe;
       } else if(factor<30) {
          // bit # to drop
          int m = ByteSequence.pcount((int)factor) - 1;

          // as bitmask
          m = 1 << m;

          // drop
          mask &= ~m;
       }

       return mask;
    }

    public int mask() {
        return mask;
    }

    public byte getByte() {
       return (byte) mask();
    }

    @Override
    public Integer get(int index) {
        return factor(index);
    }

    /**
     * Get the factor at index from the sequence.
     * @param index of the factor to return.
     * @return the number at index.
     * @throws IndexOutOfBoundsException - if the index is out of range
     */
    public int factor(int index) {
        // peek value
        long factor = sequence >> (8*index);
        factor &= 0xff;

        if(factor==0)
            throw new IndexOutOfBoundsException();

        return (int) factor;
    }

    /**
     * Return a sequence without the given factor.
     * Return this sequence again if the sequence does not contain the given factor
     * or the factor is not a prime at all.
     *
     * @param factor to expunge.
     * @return a sequence without the given factor, or this.
     */
    abstract public ByteSequence expunge(long factor);

    @Override
    public <R> R process(long start, Target<? extends R> target) {
        return from(start).process(target, 0);
    }

    /**
     * Return a truncated ByteSequence omitting all factors < start.
     * @param start limit of first prime.
     * @return a truncated ByteSequence.
     */
    abstract public ByteSequence from(long start);

    @Override
    public int count(long limit) {
        int count = 0;
        if(limit>0) {
            count = size();
            if(count<ByteSequence.SIZE)
                count -= from(limit).size();
        }

        return count;
    }

    @Override
    public <R> R process(long start, Target<? extends R> process, long offset) {
        return from(start-offset).process(process, offset);
    }

    @Override
    public <R> R process(Target<? extends R> process, long offset) {
        R result = null;

        // drop mask
        for(long values = sequence; result==null && values!=0; values >>= 8) {
            result = process.process(offset + (values & 0xff));
        }

        return result;
    }
}
