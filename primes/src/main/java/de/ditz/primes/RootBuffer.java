package de.ditz.primes;

/**
 * A RootBuffer is a template containing no numbers which are a multiple of factor.
 * Thus, the primes <= number are missing, but it contains a 1.
 * This is an extension of ByteBuffer(255) with a root factor of 5;
 */
public class RootBuffer extends BufferedSequence {

    final int root;

    RootBuffer(int size, int root) {
        super(0, size);
        this.root = root;
    }

    RootBuffer() {
        this(1, 5);
        buffer.put(0, ByteSequence.ROOT);
    }

    RootBuffer grow() {
        return process(root +1, this::grow);
    }

    RootBuffer grow(long prime) {
        return grow((int)prime);
    }

    /**
     * Grop this buffer by the next prime.
     * @param next prime to grow by.
     * @return a grown PrimeBuffer.
     */
    RootBuffer grow(int next) {
        int capacity = this.capacity();
        RootBuffer grown = new RootBuffer(next * capacity, next);

        for (BufferedSequence slice:grown.slices(capacity)) {
            slice.buffer.put(0, this.buffer, 0, capacity);
        }

        // drop all higher multiples of grown PrimeBuffer including next itself.
        this.process(grown::pdrop);

        return grown;
    }

    /**
     * drop all multiples of this.root from this sequence.
     * @param factor to apply.
     * @return this, if we are done or null to continue.
     */
    private RootBuffer pdrop(long factor) {
        long product = factor * this.root;

        if(product<limit()) {
            drop(product);
            return null;
        }

        return this;
    }

    public static RootBuffer build(long limit) {

        RootBuffer result = new RootBuffer();

        while (result.root < limit) {
            result = result.grow();
            System.out.format("%d: %,d %,d\n", result.root, result.limit(), result.count());
        }

        return result;
    }

    public static void main(String ... args) {
        RootBuffer buffer = build(11);

        buffer.sieve(buffer, buffer.root +1);
        System.out.format("%d: %,d %,d\n", buffer.root, buffer.limit(), buffer.count());

        buffer.process(Target.all(System.out::println));
    }
}
