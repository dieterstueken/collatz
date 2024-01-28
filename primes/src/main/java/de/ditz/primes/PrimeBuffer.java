package de.ditz.primes;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 22.01.24
 * Time: 11:50
 */
public class PrimeBuffer extends BufferedSequence {

    final int factor;

    PrimeBuffer(int size, int factor) {
        super(0, size);
        this.factor = factor;
    }

    PrimeBuffer() {
        this(1, 5);
        buffer.put(0, ByteSequence.ROOT);
    }

    PrimeBuffer grow() {
        return process(factor+1, this::grow);
    }

    PrimeBuffer grow(long prime) {
        return grow((int)prime);
    }

    PrimeBuffer grow(int prime) {
        int size = capacity();
        int product = prime * size;
        PrimeBuffer grown = new PrimeBuffer(product, prime);

        for (BufferedSequence slice:grown.slices(size)) {
            slice.buffer.put(0, this.buffer, 0, slice.capacity());
        }

        this.sieve(this,prime);

        grown.sieve(this).apply(prime);

        return grown;
    }

    public static PrimeBuffer build(long limit) {

        PrimeBuffer result = new PrimeBuffer();

        while (result.factor < limit) {
            result = result.grow();
            System.out.format("%d: %,d %,d\n", result.factor, result.limit(), result.count());
        }

        return result;
    }

    public static void main(String ... args) {
        PrimeBuffer buffer = build(11);

        buffer.sieve(buffer, buffer.factor+1);
        System.out.format("%d: %,d %,d\n", buffer.factor, buffer.limit(), buffer.count());

        buffer.process(Target.all(System.out::println));
    }
}
