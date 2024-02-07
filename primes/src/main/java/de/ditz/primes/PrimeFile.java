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
public class PrimeFile extends BufferedList implements AutoCloseable {

    /**
     * Block size in bytes.
     * A single byte of compacted primes represents 2*3*5=30 numbers.
     */

    public static final int BLOCK = 1<<15;

    public static PrimeFile create(File file) throws IOException {
        return new PrimeFile(BufferedFile.create(file.toPath(), BLOCK));
    }

    public static PrimeFile append(File file) throws IOException {
        return new PrimeFile(BufferedFile.append(file.toPath(), BLOCK));
    }

    public static PrimeFile open(File file) throws IOException {
        return open(file, BLOCK);
    }
    public static PrimeFile open(File file, int block) throws IOException {
        return new PrimeFile(BufferedFile.open(file.toPath(), block));
    }

    long dups = 0;

    final BufferedFile file;

    final RootBuffer root = RootBuffer.build(17);

    public PrimeFile(BufferedFile file) {
        super(new BufferCache(file));
        this.file = file;
    }

    public int size() {
        return buffers.size();
    }

    protected int blockSize() {
        return file.blockSize();
    }

    public long limit() {
        return ByteSequence.SIZE * file.length();
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    public long[] stat(long[] stat) {

        for (BufferedSequence buffer : buffers) {
            buffer.stat(stat);
        }

        return stat;
    }

    public long[] stat() {
        return stat(new long[9]);
    }


    BufferedSequence grow() {
        long base = file.length();

        long len = blockSize();

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
        dups += root.sieve(block).sieve(this).dups();

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

    void dump(String file) throws FileNotFoundException {
        dump(new File(file));
    }


    public long count() {
        return buffers.stream().mapToLong(BufferedSequence::count).sum();
    }

    public double dups() {
        long written = file.written();
        return written==0 ? 0 : 100.0 * dups / written;
    }

    public static void log(PrimeFile primes) {

        if((primes.buffers.size()%1000)==0) {
            long[] stat = primes.stat();
            System.out.format("%d %,d %,d %,.1f%% %s\n", primes.size(), primes.limit(), stat[8], primes.dups(), Arrays.toString(primes.stat()));
        } else
            System.out.format("%d %,d %,.1f%%\n", primes.size(), primes.limit(), primes.dups());
    }

    public static void main(String ... args) throws IOException {
        
        try(PrimeFile primes = PrimeFile.append(new File("primes.dat"))) {

            while(primes.buffers.size()<1024*1024*4) {
                primes.grow();
                if((primes.buffers.size()%100)==0)
                    log(primes);
            }

            System.out.println();
            log(primes);
        }
    }
}
