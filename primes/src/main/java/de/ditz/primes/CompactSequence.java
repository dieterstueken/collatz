package de.ditz.primes;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.LongPredicate;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.01.24
 * Time: 12:42
 */
public class ByteSequence implements Sequence {

    final long sequence;

    private ByteSequence(long sequence) {
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

    public int size() {
        return Integer.bitCount(mask());
    }

    public boolean forEach(LongPredicate until) {
        return forEach(until, 0);
    }

    @Override
    public boolean forEach(LongPredicate until, long offset) {

        // virtual 1
        if((sequence&1)!=0) {
            if(until.test(offset+1))
                return true;
        }

        long values = sequence>>8;
        while(values!=0) {
            long value = values & 0xff;
            if(until.test(offset+value))
                return true;
            values >>= 8;
        }

        return false;
    }

    static final List<ByteSequence> SEQUENCES ;

    public static ByteSequence sequence(int index) {
        return SEQUENCES.get(index&0xff);
    }

    private static final byte[] MASKS;

    public static byte mask(byte index, int factor) {
        byte m = MASKS[factor];
        return (byte)(index & ~m);
    }

    static {
        int[] BASE = {1,7,11,13,17,19,23,29};

        IntFunction<ByteSequence> generate = m -> {
            long sequence = 0;

           for(int i=0; i<7; ++i) {
               if((m&(0x80>>i))!=0) {
                   sequence = 256*sequence + BASE[7-i];
               }
           }

           return new ByteSequence(sequence);
        };

        SEQUENCES = IntStream.range(0, 256).mapToObj(generate).toList();

        MASKS = new byte[30];
        for(int i=0; i<BASE.length; ++i) {
            int p = BASE[i];
            MASKS[p] = (byte)(1<<i);
        }
    }
}
