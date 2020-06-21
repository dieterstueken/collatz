package de.ditz.primes;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.06.20
 * Time: 21:58
 */
interface Sequences {

    byte[] base = new byte[]{1,7,11,13,17,19,23,29};

    static Sequence compact(int index) {
        if(index>255)
            throw new IndexOutOfBoundsException();

        if(index==255)
            return sequence(base);

        byte[] seq = new byte[Integer.bitCount(index)];
        Arrays.fill(seq, (byte)0xff);
        int n = 0;
        for(int i=0; i<8; ++i) {
            int m = 1<<i;
            if((index&m)!=0)
                seq[n++] = base[i];
        }

        return sequence(seq);
    }

    static Sequence sequence(byte[] seq) {
        return (base, skip, until) -> {
            for (byte b : seq) {
                long n = base + (0xff&b);
                if (n>skip && until.test(n))
                    return true;
            }
            return false;
        };
    }
}
