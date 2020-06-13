package de.ditz.primes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.RandomAccess;
import java.util.function.LongPredicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  11.06.2020 13:46
 * modified by: $
 * modified on: $
 */
public class PrimeFile implements RandomAccess, AutoCloseable {

    BufferedFile buffers;

    final int bias;

    protected PrimeFile(BufferedFile buffers, int bias) {
        this.buffers = buffers;
        this.bias = bias;
    }

    protected PrimeFile(BufferedFile buffers) {
        this(buffers, 0);
    }

    public int size() {
        return (int)(buffers.length()/4);
    }

    private long toPrime(int value) {

        // unsigned
        long prime = 0xffffffffL & value;
        prime += (long)bias << 32;

        // odd values only
        return 2*prime + 1;
    }

    public long getPrime(int index) {
        int count = buffers.bytes() / 4;
        ByteBuffer buffer = buffers.get(index / count);
        int value = buffer.getInt(4 * (index % count));
        return toPrime(value);
    }

    public Long get(int index) {
        return getPrime(index);
    }

    public int putPrime(long prime) {
        if(prime<3 || (prime&1)==0)
            throw new IllegalArgumentException("not a prime: " + prime);

        if(prime>>33 != bias)
            throw new IllegalArgumentException("bias mismatch");

        int tail = (int)(prime/2);
        buffers.putInt(tail);

        return size();
    }

    public void flush() {
        buffers.flush();
    }

    @Override
    public void close() throws IOException {
        buffers.close();
    }

    public static PrimeFile open(Path path) throws IOException {
        BufferedFile file = BufferedFile.open(path);
        return new PrimeFile(file);
    }

    public static PrimeFile create(Path path) throws IOException {
        BufferedFile file = BufferedFileWriter.create(path);
        return new PrimeFile(file);
    }

    public static PrimeFile append(Path path) throws IOException {
        BufferedFile file = BufferedFileWriter.append(path);
        return new PrimeFile(file);
    }

    public int forEachInt(int index, LongPredicate until) {

        int count = buffers.size();
        for(int ib=index/buffers.bytes(); ib<count; ++ib) {
            ByteBuffer buffer = buffers.get(ib);

            for (int pos = index%buffers.bytes(); 4*pos < buffer.position(); ++pos) {
                int value = buffer.getInt(4 * pos);
                long prime = toPrime(value);
                if (!until.test(prime))
                    return index;
                ++index;
            }

        }

        return index;
    }
}
