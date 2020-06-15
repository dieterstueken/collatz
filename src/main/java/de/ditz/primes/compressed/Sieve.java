package de.ditz.primes.compressed;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.06.20
 * Time: 20:15
 */
public class Sieve {

    private static final byte[] MASKS = new byte[30];

    static {
        for(int i=0; i<8; ++i) {
            int b = PrimeFile.base(i);
            MASKS[b] = (byte)(1<<b);
        }
        for(int i=0; i<30; ++i)
            MASKS[i] ^= 0xff;
    }

    long base = 0;
    final byte[] bytes;
    final ByteBuffer buffer;

    Sieve(int size) {
        bytes = new byte[size];
        buffer = ByteBuffer.wrap(bytes);
    }

    void reset(long base) {
        this.base = base;
        buffer.reset();
        Arrays.fill(bytes, (byte)(0xff));
    }

    boolean clear(int ipos) {

        int pos = ipos/30;
        if(pos<buffer.limit()) {
            int mask = MASKS[ipos%30];
            bytes[pos] &= mask;
            return true;
        }

        return false;
    }

    void sieve(long prime) {
        int pos = (int)(prime-base%prime);
        for(int i=0; clear(pos + PrimeFile.base(i)); ++i) {
            pos += 2*prime;
        }
    }

}
