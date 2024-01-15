package de.ditz.primes;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.LongPredicate;
import java.util.stream.IntStream;

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
public class CompactSequence implements Sequence {

    public static int SIZE = 2*3*5;

    final long sequence;

    private CompactSequence(long sequence) {
        this.sequence = sequence;
    }

    public byte mask() {
        return (byte)(sequence&0xff);
    }

    public byte get(int i) {
        if((sequence&1)!=0) {
            if(i==0)
                return 1;
            --i;
        }

        return (byte)((sequence << 8*i)&0xff);
    }

    public int count() {
        return Integer.bitCount(mask());
    }

    @Override
    public boolean forEach(long start, LongPredicate until, long offset) {

        // fast track
        if(start <= offset)
            return forEach(until, offset);

        // sequence is skipped completely
        if(start >= offset+30)
            return false;

        // partial processing (infrequent).
        return forEach(Sequence.start(until, start), offset);
    }

    @Override
    public boolean forEach(LongPredicate until, long offset) {

        // virtual 1
        if((sequence&1)!=0) {
            if(until.test(offset+1))
                return true;
        }

        // drop mask
        long values = sequence>>8;
        while(values!=0) {
            if(until.test(offset + (values & 0xff)))
                return true;

            values >>= 8;
        }

        return false;
    }

    public CompactSequence drop(long factor) {
        if(factor>0 && factor<30)
            return clear((int)factor);
        else
            return this;
    }

    public CompactSequence clear(int factor) {
        return sequence(mask(mask(), factor));
    }

    static final List<CompactSequence> SEQUENCES ;

    public static CompactSequence sequence(int index) {
        return SEQUENCES.get(index&0xff);
    }

    private static final byte[] MASKS;

    /**
     * Drop a given factor [0,30] from the mask of factors.
     * @param index of the compact sequence.
     * @param factor to drop.
     * @return index of the reduced sequence.
     */
    public static byte mask(byte index, int factor) {

        if(factor<=0 || factor>=30)
            return index;

        byte m = MASKS[factor];
        return (byte)(index & ~m);
    }

    static {
        int[] BASE = {1,7,11,13,17,19,23,29};

        IntFunction<CompactSequence> generate = m -> {
            long sequence = 0;

           for(int i=0; i<7; ++i) {
               if((m&(0x80>>i))!=0) {
                   sequence = 256*sequence + BASE[7-i];
               }
           }

           return new CompactSequence(sequence);
        };

        SEQUENCES = IntStream.range(0, 256).mapToObj(generate).toList();

        MASKS = new byte[30];
        for(int i=0; i<BASE.length; ++i) {
            int p = BASE[i];
            MASKS[p] = (byte)(1<<i);
        }
    }
}
