package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

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

    public static PrimeFile create(File file, int block) throws IOException {
        return new PrimeFile(BufferedFile.create(file.toPath(), block));
    }

    public static PrimeFile append(File file, int block) throws IOException {
        return new PrimeFile(BufferedFile.append(file.toPath(), block));
    }

    public static PrimeFile open(File file) throws IOException {
        return open(file, BLOCK);
    }
    public static PrimeFile open(File file, int block) throws IOException {
        return new PrimeFile(BufferedFile.open(file.toPath(), block));
    }

    final BufferedFile file;

    final List<BufferedSequence> buffers = new AbstractList<> () {
        final List<BufferedSequence> cached = new ArrayList();

        @Override
        public int size() {
            return (int)((file.length + file.blockSize() - 1) / file.blockSize());
        }

        private BufferedSequence sequence(int index) {
            long offset = index*file.block*ByteSequence.SIZE;
            return new BufferedSequence(offset, file.get(index));
        }

        @Override
        public BufferedSequence get(int i) {
            BufferedSequence sequence = null;

            if(i < cached.size()) {
                sequence = cached.get(i);
            } else {
                synchronized (cached) {
                    if (i < cached.size())
                        sequence = cached.get(i);
                    else {
                        for (int k = cached.size(); k <= i; ++k) {
                            sequence = sequence(k);
                        }
                    }
                }
            }

            // verify partial sequences
            if(sequence.capacity()<file.block && sequence.limit()<file.size()) {
                // reload sequence after file grown
                sequence = sequence(i);
                cached.set(i, sequence);
            }
            
            return sequence;
        }
    };

    final RootBuffer root = RootBuffer.build(17);

    public PrimeFile(BufferedFile file) {
        this.file = file;
    }


    public int size() {
        return buffers.size();
    }

    public long limit() {
        return ByteSequence.SIZE * file.length();
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    long blocks(long start) {
        return file.blocks(ByteSequence.count(start));
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

        final long block = file.block*ByteSequence.SIZE;

        // find blocks to skip.
        int n = (int)(start/block);

        while(result == null && n<buffers.size()) {
            BufferedSequence sequence = buffers.get(n++);
            result = sequence.process(start, target);
        }

        return result;
    }

    public void write(BufferedSequence sequence) {
        write(sequence.getBuffer());
    }

    public void write(ByteBuffer buffer) {
        file.write(buffer);
    }

    BufferedSequence grow() {
        long base = file.length();

        long len;

        if(base==0) {
            // else we miss 31*31
            len = 8;
        } else if(2*base<file.block) {
            len = base;
        } else {
            len = file.block - base%file.block;
        }

        BufferedSequence block = new BufferedSequence(base, (int)len);
        root.sieve(block).sieve(this);

        write(block);

        return block;
    }

    public long count() {
        return buffers.stream().mapToLong(BufferedSequence::count).sum();
    }

    public static void main(String ... args) throws IOException {
        
        try(PrimeFile primes = PrimeFile.append(new File("primes.dat"), BLOCK)) {

            while(primes.size()<100) {
                primes.grow();
                System.out.format("%,d %,d\n", primes.limit(), primes.count());
            }

            long[] stat = primes.stat();
            for (int i = 0; i < stat.length; i++) {
                long l = stat[i];
                System.out.format("%d: %,d\n", i, l);
           }

        }
    }
}
