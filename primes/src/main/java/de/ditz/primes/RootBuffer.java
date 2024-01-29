package de.ditz.primes;

import java.util.*;

/**
 * A RootBuffer is a template containing no numbers which are a multiple of factor.
 * Thus, the primes <= number are missing, but it contains a 1.
 * This is an extension of ByteBuffer(255) with a root factor of 5;
 */
public class RootBuffer extends BufferedSequence {

    final RootBuffer root;

    final int prime;

    final List<BufferedSequence> buffers;

    RootBuffer() {
        super(0, 1);
        buffer.put(0, (byte)ByteSequence.ROOT);
        this.root = null;
        this.prime = 5;
        buffers = Collections.emptyList();
    }

    RootBuffer(RootBuffer root, int prime) {
        super(0, prime * root.capacity());
        this.root = root;
        this.prime = prime;

        root.fill(this);

        // drop all higher multiples of grown PrimeBuffers root.
        root.process(this::dropRoots);

        buffers = new Slices(root.capacity());
    }

    @Override
    public String toString() {
        return String.format("RootBuffer{%d:%d:%d}", prime, buffer.capacity(), this.limit());
    }

    RootBuffer grow() {
        return process(prime +1, this::grow);
    }

    RootBuffer grow(long prime) {
        return grow((int)prime);
    }

    /**
     * Fill up a target buffer from this root sequence.
     * The target buffer may be smaller or bigger than this and my have some odd offset.
     * @return the target buffer again.
     */
    public BufferedSequence fill(BufferedSequence target) {

        // start position
        int pos = (int)(target.base % this.capacity());

        // fill the first part partially
        if(pos!=0) {
            int length = Math.min(this.capacity()-pos, target.capacity());
            target.buffer.put(0, this.buffer, pos, length);
            pos += length;
        }

        // fill the remaining part
        while(pos<target.capacity()) {
            int length = Math.min(this.capacity(), target.capacity() - pos);
            target.buffer.put(pos, this.buffer, 0, length);
            pos += this.capacity();
        }

        return target;
    }

    /**
     * Grop this buffer by the next prime.
     * @param next prime to grow by.
     * @return a grown PrimeBuffer.
     */
    RootBuffer grow(int next) {
        return new RootBuffer(this, next);
    }

    /**
     * drop all multiples of this.root from this sequence.
     * @param factor to apply.
     * @return this, if we are done or null to continue.
     */
    private RootBuffer dropRoots(long factor) {
        long product = factor * this.prime;

        if(product<limit()) {
            drop(product);
            return null;
        }

        return this;
    }

    public static RootBuffer build(long limit) {

        RootBuffer result = new RootBuffer();

        while (result.prime < limit) {
            result = result.grow();
            System.out.format("%d: %,d %,d %,d %.1f\n",
                    result.prime, result.capacity(), result.limit(), result.count(),
                    1.0 * result.limit() / result.count());
        }

        return result;
    }

    public static void main(String ... args) {
        RootBuffer buffer = build(19);
        
        //buffer.process(Target.all(System.out::println));
    }
}
