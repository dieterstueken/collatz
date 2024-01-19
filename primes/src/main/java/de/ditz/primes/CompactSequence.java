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

    final long sequence;

    CompactSequence(long sequence, long prod) {
        super(prod);
        this.sequence = sequence;
    }

    abstract public int mask();

    /**
     * Get the factor at index from the sequence.
     * @param index of the factor to return.
     * @return the number at index.
     * @throws IndexOutOfBoundsException - if the index is out of range
     */
    public int getPrime(int index) {
        int factor = (byte) ((sequence >> (8 * index)) & 0xff);

        if(factor==0)
            throw new IndexOutOfBoundsException();

        return factor;
    }

    @Override
    public <R> R process(long start, LongFunction<? extends R> process, long offset) {
        return from(start).process(process, offset);
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

    @Override
    public ByteSequence expunge(long factor) {
        ByteSequence result = this;

        int mask = mask();

        if((factor>1) && (prod%factor) == 0) {
            // get # of prime by interpolation.
            int n = (int)(5* factor - 3)/48;
            result = Sequences.sequence(mask^(1<<n));
        } else if(factor ==1) {
            if(mask%2==1)
                result = Sequences.sequence(mask^1);
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CompactSequence(");
        sb.append("%02X".formatted(mask()));
        char c=':';
        for(int i=0; i<size(); ++i) {
            sb.append(c).append("%02d".formatted(get(i)));
            c=',';
        }
        sb.append(')');
        return sb.toString();
    }
}
