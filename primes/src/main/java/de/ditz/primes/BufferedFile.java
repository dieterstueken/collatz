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

    final int blockSize;

    final FileChannel channel;

    protected long length;

    protected long written = 0;

    protected BufferedFile(FileChannel channel, int blockSize) throws IOException {
        this.blockSize = blockSize;
        this.channel = channel;
        this.length = channel.size();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    int blockSize() {
        return blockSize;
    }

    public long length() {
        return length;
    }

    @Override
    public int size() {
        return (int)(this.length / blockSize());
    }


    public int blocks(long size) {
        return (int) (size / blockSize);
    }

    public int blocks() {
        return blocks(length + blockSize - 1);
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
            written += channel.write(buffer);
            length = channel.size();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public long written() {
        return written;
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
