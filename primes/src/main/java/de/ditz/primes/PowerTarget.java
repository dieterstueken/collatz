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

    /**
     * The target may be reset.
     * @return this for chaining.
     */
    PowerTarget<R> reset() {
        pow = 1;
        return this;
    }


    @Override
    public R apply(long factor) {
        // initial setup
        if(pow==1) {
            // grow beyond current factor
            while(pow*prime<factor)
                pow *= factor;
        }

        // todo: target = new PowerTarget(target, ++prime)

        if(pow<factor) {
            R result = target.apply(factor);
            if(result!=null)
                return result;
            pow *= factor;
        }

        return target.apply(factor);
    }
}
