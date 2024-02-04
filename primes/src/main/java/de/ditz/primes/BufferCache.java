package de.ditz.primes;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.24
 * Time: 18:52
 */
class BufferCache extends AbstractList<BufferedSequence> implements AutoCloseable {

    final BufferedFile file;

    final List<BufferedSequence> cached = new ArrayList<>();

    BufferCache(BufferedFile file) {
        this.file = file;
    }

    public long blockSize() {
        return file.blockSize;
    }

    public long length() {
        return file.length();
    }

    public long limit() {
        return ByteSequence.SIZE * file.length();
    }

    @Override
    public int size() {
        return file.blocks();
    }

    @Override
    public BufferedSequence get(int i) {
        BufferedSequence sequence = null;

        if (i < cached.size()) {
            sequence = cached.get(i);
        } else {
            synchronized (cached) {
                if (i < cached.size())
                    sequence = cached.get(i);
                else {
                    for (int k = cached.size(); k <= i; ++k) {
                        sequence = read(k);
                        cached.add(sequence);
                    }
                }
            }
        }

        // verify partial sequences
        if (sequence.capacity() < file.blockSize && sequence.limit() < limit()) {
            // reload sequence after file grown
            sequence = read(i);
            cached.set(i, sequence);
        }

        return sequence;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private BufferedSequence read(int index) {
        long base = index * blockSize();
        return new BufferedSequence(base, file.get(index));
    }

    public void write(BufferedSequence buffer) {
        if(buffer.base != file.length())
            throw new IllegalArgumentException("unexpected buffer offset");

        file.write(buffer.getBuffer());
    }
}
