package de.ditz.primes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.06.20
 * Time: 22:33
 */
public class BufferedFile extends AbstractList<ByteBuffer> implements RandomAccess, AutoCloseable {

    final int block;

    final FileChannel channel;

    protected long length;

    protected BufferedFile(FileChannel channel, int block) throws IOException {
        this.block = block;
        this.channel = channel;
        this.length = channel.size();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    int blockSize() {
        return block;
    }

    long blocks(long size) {
        return size / block;
    }

    public long length() {
        return length;
    }

    @Override
    public int size() {
        return (int)(this.length / blockSize());
    }

    @Override
    public ByteBuffer get(int index) {
        try {
            long pos = (long) index * blockSize();
            int len = (int) Math.min(length - pos, blockSize());
            ByteBuffer bytes = channel.map(FileChannel.MapMode.READ_ONLY, pos, len);
            bytes.position(bytes.limit());
            return bytes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void write(ByteBuffer buffer) {
        try {
            channel.position(length);
            int written = channel.write(buffer);
            length = channel.size();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static BufferedFile open(Path path, int block) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        return new BufferedFile(channel, block);
    }

    public static BufferedFile create(Path path, int block) throws IOException {
        return open(path, block,true);
    }

    public static BufferedFile append(Path path, int block) throws IOException {
        return open(path, block, false);
    }

    public static BufferedFile open(Path path, int block, boolean truncate) throws IOException {
        Set<StandardOpenOption> options = EnumSet.of(StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);

        if(truncate)
            options.add(StandardOpenOption.TRUNCATE_EXISTING);

        FileChannel channel = FileChannel.open(path, options);
        return new BufferedFile(channel, block);
    }
}
