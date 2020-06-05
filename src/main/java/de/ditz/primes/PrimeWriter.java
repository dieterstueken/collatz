package de.ditz.primes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.06.20
 * Time: 23:54
 */
public class PrimeWriter extends PrimeList implements AutoCloseable {

    PrimeBuffer tail = null;

    public static PrimeWriter open(Path path) throws IOException {

        FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE,
                StandardOpenOption.READ, StandardOpenOption.WRITE);

        return new PrimeWriter(channel);
    }

    protected PrimeWriter(FileChannel channel) throws IOException {
        super(channel);

        // setup any partial tail buffer
        if(!buffers.isEmpty()) {
            int l = buffers.size()-1;
            PrimeBuffer last = buffers.get(l);
            if(last.hasRemaining()) {
                ByteBuffer bytes = ByteBuffer.allocateDirect(PrimeBuffer.BYTES);
                last.bytes.rewind();
                bytes.put(last.bytes);
                tail = PrimeBuffer.buffer(bytes);
                buffers.set(l, tail);
            }
        }
    }

    public void addPrime(long prime) {
        if(tail==null) {
            tail = PrimeBuffer.create(prime);
            buffers.add(tail);
        } else {
            tail.addPrime(prime);
            if(!tail.hasRemaining()) {
                try {
                    flush();
                    tail = null;
                    PrimeBuffer buffer = map(buffers.size());
                    buffers.set(buffers.size() - 1, buffer);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    public void flush() throws IOException {
        long seek = buffers.size() * PrimeBuffer.BYTES;
        ByteBuffer buffer = tail.bytes;
        int lim = buffer.limit();
        int pos = buffer.position();
        try {
            buffer.flip();
            channel.write(buffer, seek);
            channel.force(false);
        } finally {
            buffer.position(pos);
            buffer.limit(lim);
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        channel.close();
    }
}
