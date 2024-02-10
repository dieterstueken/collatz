package de.ditz.primes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

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
    public static int ROOT = 17;

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

    long dups = 0;

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
     * @param target to preocess primes.
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

    Sieve sieve() {
        return new Sieve(root, this);
    }

    public BufferedSequence grow() {
        long base = file.length();

        long len = file.blockSize();

        if(base==0) {
            // else we miss 31*7
            len = 4;
        } else if(2*base<len) {
            len = base;
        } else {
            // fill remaining part up to next block end
            len -= base%len;
        }
        
        BufferedSequence block = new BufferedSequence(base, (int)len);
        dups += sieve().sieve(block).dups();

        // restore initial sequence
        if(base==0)
            block.buffer.put(0, (byte) Sequences.root().from(7).mask());

        file.write(block.getBuffer());

        return block;
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

    public double dups() {
        long written = file.written();
        return written==0 ? 0 : 100.0 * dups / written;
    }

    public long[] stat() {
        return buffers.stat(new long[9]);
    }

    public static void log(PrimeFile primes) {

        if((primes.size()%1000)==0) {
            long[] stat = primes.stat();
            System.out.format("%d %,d %,d %,.1f%% %s\n", primes.size(), primes.limit(), stat[8], primes.dups(), Arrays.toString(primes.stat()));
        } else
            System.out.format("%d %,d %,.1f%%\n", primes.size(), primes.limit(), primes.dups());
    }

    public static void main(String ... args) throws IOException {
        
        try(PrimeFile primes = PrimeFile.append(new File("primes.dat"))) {

            while(primes.size()<1024*1024*4) {
                primes.grow();
                if((primes.size()%100)==0)
                    log(primes);
            }

            System.out.println();
            log(primes);
        }
    }
}
