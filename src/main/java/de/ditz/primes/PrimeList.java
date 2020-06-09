package de.ditz.primes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.06.20
 * Time: 21:11
 */
public class PrimeList {

    final FileChannel channel;

    final List<PrimeBuffer> buffers = new ArrayList<>();

    public static PrimeList open(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        return new PrimeList(channel);
    }

    protected PrimeList(FileChannel channel) throws IOException {
        this.channel = channel;

        int count = (int) ((channel.size() + PrimeBuffer.BYTES - 1) / PrimeBuffer.BYTES);

        for(int i=0; i<count; ++i) {
            PrimeBuffer buffer = map(i);
            buffers.add(buffer);
        }
    }

    protected PrimeBuffer map(int index) throws IOException {
        long pos = index * PrimeBuffer.BYTES;
        int len = (int) Math.min(channel.size()-pos, PrimeBuffer.BYTES);
        ByteBuffer bytes = channel.map(FileChannel.MapMode.READ_ONLY, pos, len);
        bytes.position(bytes.limit());
        return PrimeBuffer.buffer(bytes);
    }

    public boolean forEachPrime(LongPredicate until) {
        for (PrimeBuffer buffer : buffers) {
            if(!buffer.primes(until))
                return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return buffers.isEmpty();
    }

    public long size() {
        if(buffers.isEmpty())
            return 0;

        int l = buffers.size()-1;
        long size = l * PrimeBuffer.SIZE;

        // last buffer may be partial
        size += buffers.get(l).size();

        return size;
    }

    public long getPrime(long index) {
        long n = Math.min(index/PrimeBuffer.SIZE, Integer.MAX_VALUE);
        PrimeBuffer buffer = buffers.get((int) n);
        int i = (int)(index % PrimeBuffer.SIZE);
        return buffer.getPrime(i);
    }

    public long lastPrime() {
        return buffers.isEmpty() ? 0 : getPrime(size()-1);
    }
}
