package de.ditz.primes;

import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.02.24
 * Time: 16:20
 */
public class PrimeSieve {

    public static int ROOT = 19;

    final RootBuffer root = RootBuffer.build(ROOT);

    final PrimeFile primes;

    final ForkJoinPool pool;

    final int max;

    final LinkedList<BufferSieve> sieves = new LinkedList<>();

    public PrimeSieve(PrimeFile primes, ForkJoinPool pool) {
        this.primes = primes;
        this.pool = pool;
        this.max = 2*pool.getParallelism();
    }

    public PrimeSieve(PrimeFile primes) {
        this(primes, new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true));
    }

    BufferSieve newTask() {
        return new BufferSieve(root, primes);
    }

    long submit(BufferSieve sieve, long base) {
        base = sieve.rebase(base);
        pool.submit(sieve);
        sieves.add(sieve);
        return base;
    }

    long expand(long base) {

        // submit further tasks if the number of current primes is sufficient
        double len = primes.file.length;
        len = len*len*30;

        while(sieves.size()<max && base<len) {
            base = submit(newTask(), base);
        }

        return base;
    }

    void cancel() {
        for(BufferSieve sieve = sieves.peekLast();
            sieve!=null && sieve.cancel(false);
            sieve = sieves.peekLast()) {
                sieves.pollLast();
        }
    }

    public PrimeFile growTo(long limit) {
        return grow(buffer -> primes.limit()>limit);
    }

    public PrimeFile grow(Predicate<BufferedSequence> until) {

        BufferSieve sieve = newTask();
        long base = submit(sieve, primes.file.length);

        boolean stopped = false;
        while((sieve = sieves.poll()) != null) {
            BufferedSequence sequence = sieve.join();
            primes.write(sequence);

            if(!stopped) {
                stopped = until.test(sequence);
                if(stopped) {
                    cancel();
                } else {
                    // submit last task anyway
                    base = submit(sieve, base);
                    if(sieves.size()<max)
                        base = expand(base);
                }
            }
        }

        return primes;
    }
}
