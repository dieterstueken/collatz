package de.ditz.primes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Set;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  11.06.2020 14:24
 * modified by: $
 * modified on: $
 */
public class BufferedFileWriter extends BufferedFile {

    protected BufferedFileWriter(FileChannel channel) throws IOException {
        super(channel);

        // turn into a writeable buffer.
        ByteBuffer tmp = tail;
        tail = allocate();
        tail.put(tmp);
    }

    protected ByteBuffer allocate() {
        return ByteBuffer.allocateDirect(bytes());
    }

    @Override
    public long length() {
        return (long) (size()-1) * bytes() + tail.position();
    }

    @Override
    public void putInt(int value) {
        tail.putInt(value);
        if(!tail.hasRemaining())
            flush();
    }

    /**
     * Update tail buffer.
     * This possibly extends the file.
     * The files size is recalculated.
     **/
    @Override
    public void flush() {
        int pos = tail.position();
        if(pos==0)
            return;

        try {
            // write after lat full sized buffer
            long position = length - length % BYTES;
            channel.write(tail.duplicate().flip(), position);
            // update file length
            length = channel.size();

            // tail becomes full, get a new one.
            if(!tail.hasRemaining())
                tail = allocate();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static BufferedFileWriter create(Path path) throws IOException {
        return open(path, true);
    }

    public static BufferedFileWriter append(Path path) throws IOException {
        return open(path, false);
    }

    public static BufferedFileWriter open(Path path, boolean truncate) throws IOException {
        Set<StandardOpenOption> options = EnumSet.of(StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);
        if(truncate)
            options.add(StandardOpenOption.TRUNCATE_EXISTING);
        FileChannel channel = FileChannel.open(path, options);
        return new BufferedFileWriter(channel);
    }
}
