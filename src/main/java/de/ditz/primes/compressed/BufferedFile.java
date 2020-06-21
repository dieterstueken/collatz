package de.ditz.primes.compressed;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractList;
import java.util.EnumSet;
import java.util.RandomAccess;
import java.util.Set;

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

    protected BufferedFile(FileChannel channel) throws IOException {
        this.bytes = BYTES;
        this.channel = channel;
        this.length = channel.size();
    }

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
        return (int)((this.length + bytes() - 1) / bytes());
    }

    @Override
    public ByteBuffer get(int index) {
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

    public ByteBuffer get(long index) {
        if(index<size())
            return get((int)index);

        throw new IndexOutOfBoundsException("index:" + index);
    }

    public void write(ByteBuffer buffer) {
        try {
            channel.write(buffer);
            length = channel.size();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static BufferedFile open(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        return new BufferedFile(channel);
    }

    public static BufferedFile create(Path path) throws IOException {
        return open(path, true);
    }

    public static BufferedFile append(Path path) throws IOException {
        return open(path, false);
    }

    public static BufferedFile open(Path path, boolean truncate) throws IOException {
        Set<StandardOpenOption> options = EnumSet.of(StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);

        if(truncate)
            options.add(StandardOpenOption.TRUNCATE_EXISTING);

        FileChannel channel = FileChannel.open(path, options);
        return new BufferedFile(channel);
    }
}
