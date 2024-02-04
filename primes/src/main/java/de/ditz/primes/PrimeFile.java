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

    long dup = 0;

    final BufferCache buffers;

    final RootBuffer root = RootBuffer.build(17);

    public PrimeFile(BufferedFile file) {
        this.buffers = new BufferCache(file);
    }

    public int size() {
        return buffers.size();
    }

    public long limit() {
        return buffers.limit();
    }

    @Override
    public void close() throws IOException {
        buffers.close();
    }

    public long[] stat(long[] stat) {

        for (BufferedSequence buffer : buffers) {
            buffer.stat(stat);
        }

        return stat;
    }

    public long[] stat() {
        return stat(new long[8]);
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
        R result = null;

        // substitute root block
        if(start<ByteSequence.SIZE) {
            result = Sequences.PRIMES.process(start, target);
            if(result!=null)
                return result;

            // continue after 29
            start = 30;
        }

        if(start>limit())
            return null;

        final long block = buffers.blockSize()*ByteSequence.SIZE;

        // find blocks to skip.
        int n = (int)(start/block);

        while(result == null && n<buffers.size()) {
            BufferedSequence sequence = buffers.get(n++);
            result = sequence.process(start, target);
        }

        return result;
    }

    class Sieve implements Target<BufferedSequence> {

        BufferedSequence target;

        long pow;

        @Override
        public BufferedSequence apply(long product) {
            return target.drop(pow*product);
        }

        public BufferedSequence sieve(BufferedSequence target) {
            this.target = target;
            root.fill(target);
            target.dup = 0;
            BufferedSequence result = PrimeFile.this.process(root.prime+1, this::applyPrime);
            dup += target.dup;
            return result;
        }

        BufferedSequence applyPrime(long prime) {

            if(prime*prime>=target.limit())
                return target;

            for(pow=prime; pow < target.limit(); pow *= prime) {

                if(pow>prime && pow>target.offset()) {
                    target.drop(pow);
                }

                long factor = target.offset() / prime;
                factor = Math.max(factor, prime+1);
                if(pow*factor<target.limit())
                    root.process(factor, this);
            }

            return null;
        }
    }

    BufferedSequence grow() {
        long base = buffers.length();

        long len;

        if(base==0) {
            // else we miss 31*31
            len = 8;
        } else if(2*base<buffers.blockSize()) {
            len = base;
        } else {
            len = buffers.blockSize() - base%buffers.blockSize();
        }

        Sieve sieve = new Sieve();

        BufferedSequence block = new BufferedSequence(base, (int)len);
        sieve.sieve(block);

        buffers.write(block);

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

    public static void log(PrimeFile primes) {
        //System.out.format("%d %,d %,d\n", primes.size(), primes.limit(), primes.dup);
        System.out.format("%d %,d %,d %,d\n", primes.size(), primes.limit(), primes.count(), primes.dup);
    }

    public static void main(String ... args) throws IOException {
        
        try(PrimeFile primes = PrimeFile.append(new File("primes.dat"))) {

            while(primes.buffers.size()<1024) {
                primes.grow();
                log(primes);
            }

            System.out.println();

            long[] stat = primes.stat();
            System.out.println(Arrays.toString(stat));
        }
    }
}
