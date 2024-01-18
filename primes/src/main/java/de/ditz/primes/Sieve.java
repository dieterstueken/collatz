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

    public Sieve(Sequence primes, long start) {
        this.primes = primes;
        this.start = start;
    }

    public BufferedSequence sieve(BufferedSequence sequence, long offset) {

        final long limit = sequence.limit() + offset;

        BufferedSequence result = primes.process(start, p0 -> {
            long skip = offset / p0;
            if(skip<p0)
                skip = p0;

            if(skip*p0 >= limit)
                return sequence;

            primes.process(skip, p1 -> {
                long product = p0 * p1;
                if (product > limit)
                    return sequence;

                boolean dropped = sequence.drop(product - offset);

                System.out.format("%2d x %2d = %3d %s\n", p0, p1, product, dropped ? "!":"");

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
