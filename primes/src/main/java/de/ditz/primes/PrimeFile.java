package de.ditz.primes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
    public static int ROOT = 19;

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

    final RootBuffer root = RootBuffer.build(ROOT);

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

    public PrimeFile slow(Predicate<BufferedSequence> until) {

        while(true) {
            if(until.test(grow()))
                break;
        }

        return this;
    }

    private SieveTask newTask() {
        Sieve sieve = new Sieve(this);
        return new SieveTask(sieve);
    }

    public PrimeFile grow(Predicate<BufferedSequence> until) {
        return new SieveTasks(this).run(until);
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

    public PrimeFile growTo(long limit) {
        grow(buffer -> limit()>limit);
        return this;
    }

    private BufferedSequence grow() {
        long base = file.length();

        long len = file.blockSize();

        if(base==0) {
            // else we miss 31*31
            len = 8;
        } else if(2*base<len) {
            len = base;
        } else {
            // fill remaining part up to next block end
            len -= base%len;
        }

        BufferedSequence block = new BufferedSequence(base, (int)len);
        new Sieve(this).sieve(block);

        write(block);

        return block;
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

    public boolean log(BufferedSequence buffer) {

        if ((size() % 100) == 0) {
            double size = 8 * buffer.capacity() / 100.0;
            System.out.format("%d %,20d %5.1f%% %5.1f%% %5.1f\n",
                    size(), limit(), buffer.count() / size, buffer.dups() / size, Math.log(limit())/Math.log(2));
        }

        return size() >= 1024 * 1024 * 4;
    }

    public static void main(String ... args) throws IOException {
        
        try(PrimeFile primes = PrimeFile.append(new File("primes.dat"))) {

            primes.grow(primes::log);
        }
    }
}
