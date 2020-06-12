package de.ditz.primes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.RandomAccess;
import java.util.function.LongPredicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  11.06.2020 13:46
 * modified by: $
 * modified on: $
 */
public class IntFile extends AbstractList<Integer> implements RandomAccess, AutoCloseable {

    final BufferedFile buffers;

    protected IntFile(BufferedFile buffers) {
        this.buffers = buffers;
    }

    public int size() {
        return (int)(buffers.length()/4);
    }

    public int getInt(int index) {
        int ints = buffers.bytes() / 4;
        ByteBuffer buffer = buffers.get(index / ints);
        return buffer.getInt(4*(index % ints));
    }

    @Override
    public Integer get(int index) {
        return getInt(index);
    }

    public int putInt(int value) {
        buffers.putInt(value);
        return size();
    }

    public void flush() {
        buffers.flush();
    }

    @Override
    public void close() throws IOException {
        buffers.close();
    }

    public static IntFile open(Path path) throws IOException {
        BufferedFile file = BufferedFile.open(path);
        return new IntFile(file);
    }

    public static IntFile create(Path path) throws IOException {
        BufferedFile file = BufferedFileWriter.create(path);
        return new IntFile(file);
    }

    public int forEachInt(int index, LongPredicate until) {

        int count = buffers.size();
        for(int ib=index/buffers.bytes(); ib<count; ++ib) {
            ByteBuffer buffer = buffers.get(ib);

            for (int pos = index%buffers.bytes(); 4*pos < buffer.position(); ++pos) {
                int value = buffer.getInt(4 * pos);
                if (!until.test(value))
                    return index;
                ++index;
            }

        }

        return index;
    }
}
