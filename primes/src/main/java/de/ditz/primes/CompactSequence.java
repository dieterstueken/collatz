package de.ditz.primes;

import java.util.*;
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
        return sequence(mask(mask(), factor));
    }

    public CompactSequence from(long start) {

        if(start<=1)
            return this;

        if(start>=30) // skip all
            return empty();

        int m = MASKS[(int)start];
        m = (m-1) + (m&1); // 0x80 -> 0x7F, 0x7F -> 0x7F

        int mask = this.mask();
        if((mask&m)==0) // nothing to drop
            return this;

        mask &= m&0xff;
        return sequence(mask);
    }

    private static final int[] MASKS = new int[30];

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
    public static byte mask(int index, int factor) {

        if(factor>0 && factor<30) {
            int m = MASKS[factor];
            if(m%2 == 0) // odd masks are ignored
                index &= m^0xff;
        }

        return (byte)index;
    }

    static {

        int[] BASE = {1,7,11,13,17,19,23,29};

        // ..,1,..,7,..,11,..,13,..,17,..,19,..,23,..,29
        //  0,1...,2,03,04,07,08,0F,10,1F,20,3F,40,7F,80

        for(int i=0, n=0; i<BASE.length; ++i) {
            int p = BASE[i];
            int m = (1<<i)-1;
            while(n<p)
                MASKS[n++] = m;
            MASKS[p] = m+1;
            ++n;
        }

        CompactSequence empty = new CompactSequence(0) {

            @Override
            public int get(int index) {
                throw new IndexOutOfBoundsException();
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public <R> R process(long start, LongFunction<? extends R> process, long offset) {
                return null;
            }

            @Override
            public <R> R process(LongFunction<? extends R> process, long offset) {
                return null;
            }

            @Override
            public CompactSequence drop(int factor) {
                return this;
            }

            @Override
            public CompactSequence from(long start) {
                return this;
            }
        };

        // setup sequences.
        CompactSequence[] sequences = new CompactSequence[256];

        sequences[0] = empty;

        // 8 single track sequences
        for(int i=0; i<BASE.length; ++i) {
            int p = BASE[i];
            long sequence = 1<<i;
            if(i>0)
                sequence |= p<<8;

            sequences[1<<i] = new CompactSequence(sequence) {

                @Override
                public int get(int index) {
                    if(index==0)
                        return p;

                    throw new IndexOutOfBoundsException();
                }

                @Override
                public int size() {
                    return 1;
                }

                @Override
                public <R> R process(long start, LongFunction<? extends R> process, long offset) {
                    return start>p ? null : process.apply(p+offset);
                }

                @Override
                public <R> R process(LongFunction<? extends R> process, long offset) {
                    return process.apply(p+offset);
                }

                @Override
                public CompactSequence drop(int factor) {
                    return factor==p ? empty : this;
                }

                @Override
                public CompactSequence from(long start) {
                    return p<start ? empty : this;
                }
            };
        }

        // generate 256 remaining sequences
        for(int m=0; m<256; ++m) {
            if(sequences[m]!=null)
                continue;

            long sequence = 0;

            for(int i=7; i>0; --i) {
                if(((m>>i)&1) != 0) {
                    sequence = (sequence<<8) + BASE[i];
                }
            }

            sequence = (sequence<<8) | (m&0xff);
            sequences[m] = new CompactSequence(sequence);
        }

        SEQUENCES = List.of(sequences);
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

    public static void main(String ... args) {
        SEQUENCES.forEach(System.out::println);
    }
}
