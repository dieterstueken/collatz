package de.ditz.primes;

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.06.20
 * Time: 21:59
 */
class PrimeBuffer extends AbstractList<Long> {

    public static final int BYTES = 4096;
    public static final int SIZE = BYTES/2 - 3;

    final long base;

    final ByteBuffer bytes;

    public static PrimeBuffer create(long base) {
        ByteBuffer bytes = ByteBuffer.allocateDirect(BYTES);
        bytes.putLong(base);
        return new PrimeBuffer(base, bytes);
    }

    public static PrimeBuffer buffer(ByteBuffer bytes) {
        long base = bytes.getLong(0);
        return new PrimeBuffer(base, bytes);
    }

    private PrimeBuffer(long base, ByteBuffer bytes) {
        this.base = base;
        this.bytes = bytes;
    }

    @Override
    public int size() {
        return (bytes.position()-6)/2;
    }

    long getPrime(int index) {
        if(index==0)
            return base;

        if(index<0 || 2*index >= bytes.position()-6)
            throw new IndexOutOfBoundsException();

        short value = bytes.getShort(6 + 2*index);
        return base + (0xffff & value);
    }

    long lastPrime() {
        return getPrime(size()-1);
    }

    boolean addPrime(long value) {
        value -= base;
        long trunc = value & 0xffff;

        if (trunc != value)
            throw new IllegalArgumentException("value overflow");

        bytes.putShort((short) trunc);
        
        return bytes.hasRemaining();
    }

    @Override
    public Long get(int index) {
        return getPrime(index);
    }

    public boolean hasRemaining() {
        return bytes.hasRemaining();
    }

    public boolean primes(LongPredicate until) {
        for(int i=0; i<size(); ++i) {
            if (!until.test(get(i)))
                return false;
        }

        return true;
    }
}
