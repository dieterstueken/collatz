package de.ditz.primes;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.06.20
 * Time: 20:15
 */
public class Sieve {

    /**
     * Block size in bytes.
     * A single byte of compacted primes represents 2*3*5=30 numbers.
     */
    public static final int BLOCK = 7*11*13*17;

    final BufferedSequence sequence;

    Sieve(BufferedSequence sequence) {
        this.sequence = sequence;
    }
}
