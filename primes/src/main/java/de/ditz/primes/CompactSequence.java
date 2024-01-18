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
abstract public class CompactSequence implements Sequence {

    public static int SIZE = 2*3*5;

    public static long PROD = 7*11*13*17*19*23*29;

    final long sequence;

    final long prod;

    CompactSequence(long sequence, long prod) {
        this.sequence = sequence;
        this.prod = prod;
    }

    public byte mask() {
        return (byte)(sequence&0xff);
    }

    /**
     * Get the factor at index from the sequence.
     * @param index of the factor to return.
     * @return the number at index.
     * @throws IndexOutOfBoundsException - if the index is out of range
     */
    public int get(int index) {
        int factor = 0;

        if((sequence&1)!=0) {
            if(index==0)
                return 1;
        } else {
            ++index;
        }

        if (index>0 && index < 8) {
            factor = (byte) ((sequence >> (8 * index)) & 0xff);
        }

        if(factor==0)
            throw new IndexOutOfBoundsException();

        return factor;
    }

    public int size() {
        final byte mask = mask();
        return Integer.bitCount(mask&0xff);
    }

    public static long count(long size) {
        return size<0 ? 0 : size/SIZE;
    }

    @Override
    public <R> R process(long start, LongFunction<? extends R> process, long offset) {
        return from(start).process(process, offset);
    }

    @Override
    public <R> R process(LongFunction<? extends R> process, long offset) {

        R result = null;

        // virtual 1
        if((sequence&1)!=0) {
            result = process.apply(offset+1);
        }

        // drop mask
        for(long values = sequence>>8; result==null && values!=0; values >>= 8) {
            result = process.apply(offset + (values & 0xff));
        }

        return result;
    }

    public CompactSequence drop(long factor) {
        return drop((int)factor);
    }

    public CompactSequence drop(int factor) {
        int mask = mask();

        if(factor>1 && prod%factor==0) {
            // get # of prime by interpolation.
            int n = (5*factor-3)/48;
            return Sequences.sequence(mask^(1<<n));
        } else if(factor==1) {
            if(mask%2==1)
                return Sequences.sequence(mask^1);
        }

        return this;
    }

    /**
     * Drop a given factor [0,30] from a mask of factors.
     * @param mask of the compact sequence.
     * @param factor to drop.
     * @return reduced prime factor mask.
     */
    public static int drop(int mask, int factor) {

       if(factor>0 && PROD%factor==0) {
            // get # of prime by interpolation.
            int n = (5*factor-3)/48;
            mask &= 0xff & (1<<n);
        } else if(factor==1) {
            mask &= 0xfe;
        }

        return mask;
    }

    abstract public CompactSequence from(long start);

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
