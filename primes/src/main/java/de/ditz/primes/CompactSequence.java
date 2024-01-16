package de.ditz.primes;

import java.util.*;
import java.util.function.LongFunction;
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

    /**
     * Get the factor at index from the sequence.
     * @param index of the factor to return.
     * @return the number at index.
     * @throws IndexOutOfBoundsException - if the index is out of range
     */
    public int get(int index) {
        int factor = 0;

        if (index>1 && index < 7) {
            factor = (byte) ((sequence >> 8 * (index + 1)) & 0xff);
        } else if(index == 1 && (sequence&1)!=0) {
            factor = 1;
        }

        if(factor==0)
            throw new IndexOutOfBoundsException();

        return factor;
    }

    public int count() {
        final byte mask = mask();
        return Integer.bitCount(mask&0xff);
    }

    public static long count(long size) {
        return size<0 ? 0 : size/SIZE;
    }

    @Override
    public <R> R process(long start, LongFunction<? extends R> process, long offset) {

        // fast track
        if(start <= offset)
            return process(process, offset);

        // sequence is skipped completely
        if(start >= offset+30)
            return null;

        // partial processing (infrequent).
        return process(Sequence.start(process, start), offset);
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
        if(factor>0 && factor<30)
            return drop((int)factor);
        else
            return this;
    }

    public CompactSequence drop(int factor) {
        return sequence(mask(mask(), factor));
    }

    private static final byte[] MASKS = new byte[30];

    static final List<CompactSequence> SEQUENCES ;

    public static CompactSequence sequence(int index) {
        return SEQUENCES.get(index&0xff);
    }

    static CompactSequence root() {
        return sequence(255);
    }

    static CompactSequence empty() {
        return sequence(0);
    }

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

        // setup 8 of the MASK entries with the bit to clear.
        for(int i=0; i<BASE.length; ++i) {
            int p = BASE[i];
            MASKS[p] = (byte)(1<<i);
        }

        // generate 256 sequences 
        SEQUENCES = IntStream.range(0, 256).mapToObj(m -> {
            long sequence = 0;

           for(int i=7; i>0; --i) {
               if(((m>>i)&1) != 0) {
                   sequence = (sequence<<8) + BASE[i];
               }
           }

           sequence = (sequence<<8) | (m&0xff);

           return new CompactSequence(sequence);
        }).toList();
    }

    @Override
    public String toString() {
        return String.format("CompactSequence(%x)", sequence);
    }

    public static void main(String ... args) {
        SEQUENCES.forEach(System.out::println);
    }
}
