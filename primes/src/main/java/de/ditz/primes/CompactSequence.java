package de.ditz.primes;

import java.util.function.LongFunction;

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

    final int mask;

    final long sequence;

    int size;

    CompactSequence(int mask, long prod, long sequence) {
        super(prod);
        this.sequence = sequence;
        this.mask = mask;
        this.size = Integer.bitCount(mask);
    }

    @Override
    public int size() {
        return size;
    }

    public int mask() {
        return mask;
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

    @Override
    public <R> R process(long start, LongFunction<? extends R> process, long offset) {
        return from(start-offset).process(process, offset);
    }

    @Override
    public <R> R process(LongFunction<? extends R> process, long offset) {
        R result = null;

        // drop mask
        for(long values = sequence; result==null && values!=0; values >>= 8) {
            result = process.apply(offset + (values & 0xff));
        }

        return result;
    }
}
