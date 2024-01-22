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
        buffer.put(0, Sequences.ROOT.getByte());
    }

    PrimeBuffer grow() {
        return process(factor+1, this::grow);
    }

    PrimeBuffer grow(long prime) {
        return grow((int)prime);
    }

    PrimeBuffer grow(int prime) {
        int size = buffer.capacity();
        int product = prime * size;
        PrimeBuffer grown = new PrimeBuffer(product, prime);

        for (int i = 0; i < prime; ++i) {
            BufferedSequence slice = grown.slice(i*size, size);
            slice.buffer.put(0, this.buffer, 0, size);
            if(i>0)
                slice.sieve(grown, prime);
        }

        return grown;
    }

    public static PrimeBuffer build(long limit) {

        PrimeBuffer result = new PrimeBuffer();

        while (result.factor < limit)
            result = result.grow();

        return result;
    }

    public static void main(String ... args) {
        PrimeBuffer sequence = build(11);

        System.out.format("%,d %,d\n", sequence.limit(), sequence.count());

        //sequence.process(Sequence.all(System.out::println));
    }
}
