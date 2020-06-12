package de.ditz.primes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.06.20
 * Time: 22:33
 */
public class BufferedFile extends AbstractList<ByteBuffer> implements RandomAccess, AutoCloseable {

    // default value
    static final int BYTES = 1<<20;

    final int bytes;

    final FileChannel channel;

    protected long length;

    private final List<ByteBuffer> buffers = new ArrayList<>();

    ByteBuffer tail;

    protected BufferedFile(FileChannel channel) throws IOException {
        this.bytes = BYTES;
        this.channel = channel;
        this.length = channel.size();
        this.tail = map(this.size()-1);
    }

    public void flush() { }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    int bytes() {
        return bytes;
    }

    public long length() {
        return length;
    }

    @Override
    public int size() {
        // including possibly empty tail.
        return 1 + (int)(this.length / bytes());
    }

    protected ByteBuffer map(int index) {
        try {
            long pos = (long) index * bytes();

            int len = (int) Math.min(length - pos, bytes());
            ByteBuffer bytes = channel.map(FileChannel.MapMode.READ_ONLY, pos, len);
            bytes.position(bytes.limit());
            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected ByteBuffer findBuffer(int index) {
        if(index==size()-1)
            return tail;

        return index < buffers.size() ? buffers.get(index) : null;
    }

    @Override
    public ByteBuffer get(int index) {
        ByteBuffer buffer = findBuffer(index);
        if (buffer != null)
            return buffer;

        synchronized (buffers) {
            // double check
            buffer = findBuffer(index);
            if (buffer != null)
                return buffer;

            // extend list on demand
            while (buffers.size() <= index)
                buffers.add(null);

            buffer = map(index);
            buffers.set(index, buffer);
        }

        return buffer;
    }

    public void putInt(int value) {
        throw new UnsupportedOperationException("read only");
    }

    public static BufferedFile open(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        return new BufferedFile(channel);
    }
}
