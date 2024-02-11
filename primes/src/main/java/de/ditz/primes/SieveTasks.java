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
public class SieveTasks {

    final PrimeFile primes;

    final ForkJoinPool pool;

    final int max;

    final LinkedList<SieveTask> tasks = new LinkedList<>();

    public SieveTasks(PrimeFile primes, ForkJoinPool pool) {
        this.primes = primes;
        this.pool = pool;
        this.max = 2*pool.getParallelism();
    }

    public SieveTasks(PrimeFile primes) {
        this(primes, new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true));
    }

    SieveTask newTask() {
        Sieve sieve = new Sieve(primes);
        return new SieveTask(sieve);
    }

    long submit(SieveTask task, long base) {
        base = task.rebase(base);
        pool.submit(task);
        tasks.add(task);
        return base;
    }

    long submit(long base) {
        return submit(newTask(), base);
    }

    void cancel() {
        for(SieveTask task = tasks.peekLast();
            task!=null && task.cancel(false);
            task = tasks.peekLast()) {
                tasks.pollLast();
        }
    }

    public PrimeFile run(Predicate<BufferedSequence> until) {

        SieveTask task = newTask();
        long base = submit(task, primes.file.length);

        boolean stopped = false;
        while((task = tasks.poll()) != null) {
            BufferedSequence sequence = task.join();
            primes.write(sequence);

            if(!stopped) {
                stopped = until.test(sequence);
                if(stopped)
                    cancel();
            }

            if(!stopped) {
                // submit last task anyway
                base = submit(task, base);

                // submit further tasks if the number of current primes is sufficient
                double len = primes.file.length;
                len = len*len*30;

                while(tasks.size()<max && base<len) {
                    base = submit(newTask(), base);
                }
            }
        }

        return primes;
    }
}
