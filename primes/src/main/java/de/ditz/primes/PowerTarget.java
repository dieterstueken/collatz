package de.ditz.primes;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 23.01.24
 * Time: 23:03
 */

/**
 * Merge additional factors of prime^n into the incoming factors.
 * Assume factors are increasing.
 */
public class PowerTarget<R> implements Target<R> {

    final Target<R> target;

    long prime;

    long pow = 1;

    public PowerTarget(Target<R> target, long prime) {
        this.target = target;
        this.prime = prime;
    }

    public PowerTarget(Target<R> target) {
        this(target, 1);
    }

    /**
     * The target may be reset.
     * @return this for chaining.
     */
    PowerTarget<R> reset() {
        pow = 1;
        return this;
    }

    PowerTarget<R> reset(long prime) {
        this.prime = prime;
        this.pow = 1;
        return this;
    }

    @Override
    public R apply(final long factor) {

        if(pow<factor) {
            // initial setup
            if(pow<=1) {
                if(prime<=1 || pow<1)
                    throw new IllegalStateException("not initialized");

                // grow beyond current factor
                while(pow<=factor)
                    pow *= prime;
            } else {
                // apply power previously skipped
                R result = target.apply(pow * factor);
                if (result != null)
                    return result;

                pow *= prime;
            }
        }

        R result = target.apply(prime*factor);

        return result;
    }
}
