package de.ditz.primes;

import java.util.concurrent.RecursiveTask;

public class SieveTask extends RecursiveTask<BufferedSequence> {

    final Sieve sieve;

    public SieveTask(Sieve sieve) {
        this.sieve = sieve;
    }

    long rebase(long base) {
        if(this.isDone())
            reinitialize();

        sieve.rebase(base);
        return sieve.length();
    }

    @Override
    protected BufferedSequence compute() {
        BufferedSequence result = sieve.sieve();
        return result;
    }
}
