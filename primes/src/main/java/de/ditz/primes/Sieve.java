package de.ditz.primes;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.06.20
 * Time: 20:15
 */
public class Sieve {

    final Sequence primes;

    final long start;

    final long prod;

    public Sieve(Sequence primes, long start, long prod) {
        this.primes = primes;
        this.start = start;
        this.prod = prod;
    }

    public BufferedSequence sieve(BufferedSequence sequence, long offset) {

        final long limit = sequence.limit() + offset;

        BufferedSequence result = primes.process(start, p0 -> {
            long skip = 1 + Math.max(offset, prod) / p0;
            if(skip<p0)
                skip = p0;

            if(skip*p0 >= limit)
                return sequence;

            primes.process(skip, p1 -> {
                long product = p0 * p1;
                if (product > limit)
                    return sequence;

                boolean dropped = sequence.drop(product - offset);

                if(dropped)
                    return null;
                else
                    return null;
            });

            return null;
        });

        if(result==null)
            throw new IllegalStateException("primes under run");

        return result;
    }
}
