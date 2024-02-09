package de.ditz.primes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A RootBuffer is a template containing no numbers which are a multiple of factor.
 * Thus, the primes <= number are missing, but it contains a 1.
 * This is an extension of ByteBuffer(255) with a root factor of 5;
 */
public class RootBuffer extends BufferedSequence {

    final RootBuffer root;

    final int prime;

    final List<BufferedSequence> buffers;

    private RootBuffer() {
        super(0, 1);
        buffer.put(0, (byte)ByteSequence.ROOT);
        this.root = null;
        this.prime = 5;
        buffers = Collections.emptyList();
    }

    private RootBuffer(RootBuffer root, int prime) {
        super(0, prime * root.capacity());
        this.root = root;
        this.prime = prime;
        this.buffers = new Slices(root.capacity());
    }

    @Override
    public String toString() {
        return String.format("RootBuffer{%d:%d:%d}", prime, capacity(), limit());
    }

    /**
     * Get the sequence at index%capacity.
     * This results in an infinite sequence.
     *
     * @param index of ByteSequence to get.
     * @return a ByteSequence at index%capacity.
     */
    protected ByteSequence sequence(long index) {
        int m = 0xff & buffer.get((int)(index%buffer.capacity()));
        return Sequences.sequence(m);
    }

    /**
     * Fill up a target buffer from this root sequence.
     * The target buffer may be smaller or bigger than this and my have some odd offset.
     * @return the target buffer again.
     */
    public BufferedSequence fill(BufferedSequence target) {

        // start at this.buffer's position
        int pos = (int)(target.base % this.capacity());

        // fill the first partial part from this.buffer@pos
        if(pos!=0) {
            int length = Math.min(this.capacity()-pos, target.capacity());
            target.buffer.put(0, this.buffer, pos, length);
            // switch to target.buffer's position
            pos = length;
        }

        // fill the remaining part from repeating this.buffer
        while(pos<target.capacity()) {
            int length = Math.min(this.capacity(), target.capacity()-pos);
            target.buffer.put(pos, this.buffer, 0, length);
            pos += length;
        }

        return target;
    }

    private RootBuffer grow() {
        return process(prime+1, this::grow);
    }

    private RootBuffer grow(long prime) {
        RootBuffer grown = new RootBuffer(this, (int) prime);
        fill(grown);
        // drop all n*prime including 1*prime itself
        this.process(p -> grown.process(p*prime));
        return grown;
    }
    
    public static RootBuffer build(long limit) {

        RootBuffer result = new RootBuffer();

        while (result.prime < limit) {
            result = result.grow();

        }

        return result;
    }

    public static void main(String ... args) {
        RootBuffer result = build(19);

        System.out.format("%d: %,d %,d %,d %.1f\n",
                result.prime, result.capacity(), result.limit(), result.count(),
                1.0 * result.limit() / result.count());

        System.out.println(Arrays.toString(result.stat()));
    }
}
