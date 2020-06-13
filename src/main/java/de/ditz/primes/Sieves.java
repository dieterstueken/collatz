package de.ditz.primes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.06.20
 * Time: 18:46
 */
public class Sieves {

    public static final int SIZE = 1<<16;

    final PrimeWriter primes;

    final int parallel;

    final long seed;
    int count=0;

    public Sieves(PrimeWriter primes, int parallel) {
        this.primes = primes;
        this.parallel = parallel;
        this.seed = primes.lastPrime()+2;
    }

    public Sieves(PrimeWriter primes) {
        this(primes,21*Runtime.getRuntime().availableProcessors()/2);
    }

    ConcurrentLinkedQueue<Sieve> sieves = new ConcurrentLinkedQueue<>();

    private Sieve getSieve() {
        Sieve sieve = sieves.poll();
        return sieve!=null ? sieve : new Sieve();
    }

    private void releaseSieve(Sieve sieve) {
        sieves.offer(sieve);
    }

    class Task extends RecursiveTask<Sieve> {

        final int id;

        Task(int id) {
            this.id = id;
        }

        @Override
        protected Sieve compute() {
            Sieve sieve = getSieve();
            long base = seed + 2L * id * SIZE;
            sieve.reset(base, SIZE);
            sieve.sieve(primes);
            return sieve;
        }

        public int finish() {
            Sieve sieve = join();
            long n = sieve.sieve(primes);
            if(n>0)
                n = 0;

            int k = sieve.extract(primes::addPrime);
            setRawResult(null);
            releaseSieve(sieve);
            return k;
        }
    }

    public ForkJoinTask<Runnable> nextTask() {

        int i0 = parallel * count;
        ++count;

        return new RecursiveTask<>() {
            protected Runnable compute() {
                List<Task> tasks = new ArrayList<>(parallel);

                for (int i = 0; i < parallel; ++i)
                    tasks.add(new Task( i0 + i));

                //tasks.forEach(ForkJoinTask::fork);

                ForkJoinTask.invokeAll(tasks);

                return () -> {
                    tasks.forEach(Task::finish);
                    tasks.clear();
                };
            }
        };
    }
}
