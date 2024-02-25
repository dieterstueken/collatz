package de.ditz.primes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Predicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  14.06.2020 14:32
 * modified by: $
 * modified on: $
 */
public class PrimeFile implements Sequence, AutoCloseable {

    /**
     * Block size in bytes.
     * A single byte of compacted primes represents 2*3*5=30 numbers.
     */

    public static int BLOCK = 1<<15;

    public static PrimeFile create(File file) throws IOException {
        return open(BufferedFile.create(file.toPath(), BLOCK));
    }

    public static PrimeFile append(File file) throws IOException {
        return open(BufferedFile.append(file.toPath(), BLOCK));
    }

    public static PrimeFile open(File file) throws IOException {
        return open(file, BLOCK);
    }
    public static PrimeFile open(File file, int block) throws IOException {
        return open(BufferedFile.open(file.toPath(), block));
    }

    public static PrimeFile open(BufferedFile file) {

        return new PrimeFile(file);
    }

    final BufferedFile file;

    final BufferedList buffers;

    public PrimeFile(BufferedFile file) {
        this.file = file;

        BufferCache cache = new BufferCache(file);

        this.buffers = new BufferedList(cache) {

            public int blockSize() {
                return file.blockSize();
            }

            public long limit() {
                return ByteSequence.SIZE * file.length();
            }
        };
    }

    public int size() {
        return buffers.size();
    }

    public long limit() {
        return buffers.limit();
    }

    public long count() {
        return count(limit());
    }

    public long count(long limit) {

        // add 2,3,5
        if(limit>5)
            return buffers.count(limit) + 3;

        return Sequences.PRIMES.count(limit);
    }

    public List<BufferedSequence> buffers() {
        return buffers.buffers;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    /**
     * Emit primes to a target processor.
     * The root block misses primes below 17, so the first block is substituted.
     *
     * @param start first prime to emit.
     * @param target to process primes.
     * @return a target result or null if exceeded.
     */
    @Override
    public <R> R process(long start, Target<? extends R> target) {

        // substitute root block
        if(start<ByteSequence.SIZE) {
            R result = Sequences.PRIMES.process(start, target);
            if(result!=null)
                return result;

            // continue after 29
            start = 30;
        }

        // delegate to the buffers.
        return buffers.process(start, target);
    }

    public PrimeSieve sieve() {
        return new PrimeSieve(this);
    }

    public int bufferSize(long base) {

        int len = file.blockSize();

        if(base==0) {
            // else we miss 31*31
            len = 8;
        } else if(2*base<len) {
            len = (int)base;
        } else {
            // fill remaining part up to next block end
            len -= base%len;
        }

        return len;
    }

    public void write(BufferedSequence buffer) {

        if(buffer.base != file.length())
            throw new IllegalArgumentException("illegal buffer offset");

        file.write(buffer.getBuffer());
    }

    void dump(PrintWriter out) {
        process(Target.all(out::println));
    }

    void dump(File file) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(file)) {
            dump(out);
        }
    }

    public void dump(String file) throws FileNotFoundException {
        dump(new File(file));
    }

    public long[] stat() {
        return buffers.stat(new long[9]);
    }



    class Log implements Predicate<BufferedSequence> {

        static final long INTERVALL = 10000;

        long last = System.currentTimeMillis() - 9000;

        long count = 0;

        @Override
        public boolean test(BufferedSequence buffer) {

            count += buffer.count();

            long now = System.currentTimeMillis();

            if (now > last + INTERVALL) {
                last = now;

                double size = 8 * buffer.capacity() / 100.0;
                System.out.format("%,10d %,20d %,18d %5.1f%% %5.1f%% %5.1f\n",
                        size(), limit(), count, buffer.count() / size,
                        buffer.dups() / size, Math.log(limit())/Math.log(2));
            }

            return size() >= 1024 * 1024 * 4;
        }
    }

    public static void main(String ... args) throws IOException {

        String path = args.length>0 ? args[0] : "primes.dat";
        File file = new File(path);

        try(PrimeFile primes = PrimeFile.create(file)) {
            Log log = primes.new Log();
            primes.sieve().grow(log);
        }
    }
}
