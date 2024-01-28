package de.ditz.primes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  14.06.2020 14:32
 * modified by: $
 * modified on: $
 */
public class PrimeFile extends AbstractList<BufferedSequence> implements Sequence, RandomAccess, AutoCloseable {

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

    final List<BufferedSequence> sequences = new ArrayList<>();

    final RootBuffer root = RootBuffer.build(17);

    public PrimeFile(BufferedFile file) {
        this.file = file;
    }

    @Override
    public int size() {
        return file.size();
    }

    public long limit() {
        return ByteSequence.SIZE * file.length();
    }

    @Override
    public BufferedSequence get(int i) {
        if(i<sequences.size())
            return sequences.get(i);

        synchronized (sequences) {
            if(i<sequences.size())
                return sequences.get(i);

            BufferedSequence sequence = null;

            for(int k=sequences.size(); k<=i; ++k) {
                long offset = i*file.block*ByteSequence.SIZE;
                sequence = new BufferedSequence(offset, file.get(k));
                if(sequence.buffer.capacity()==file.blockSize())
                    sequences.add(sequence);
            }

            return sequence;
        }

    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    long blocks(long start) {
        return file.blocks(ByteSequence.count(start));
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

        while(result == null && n<size()) {
            BufferedSequence sequence = this.get(n++);
            result = sequence.process(start, target);
        }

        return result;
    }

    public void write(BufferedSequence sequence) {
        write(sequence.getBuffer());
    }

    public void write(ByteBuffer buffer) {

        int size = sequences.size();
        long count = file.length / file.blockSize();

        // truncate after last complete buffer
        if(size>count) {
            sequences.remove(--size);
        }

        file.write(buffer);
    }

    void grow() {
        long base = file.length();
        long len = base<file.block ? Math.min(16*Math.max(1, base), file.block) - base: file.block - base%file.block;

        BufferedSequence block = new BufferedSequence(base, (int)len);
        root.fill(block);
        block.sieve(this, root.root+1);

        write(block);
    }

    public long count() {
        return stream().mapToLong(BufferedSequence::count).sum();
    }

    public static void main(String ... args) throws IOException {
        
        try(PrimeFile primes = PrimeFile.append(new File("primes.dat"), BLOCK)) {
            while(primes.size()<4) {
                primes.grow();
                System.out.format("%,d %,d\n", primes.limit(), primes.count());
            }

            primes.process(Target.all(System.out::println));
        }
    }
}
