package de.ditz.primes;

import java.util.function.LongFunction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.06.20
 * Time: 20:42
 */
public interface Sequence {

    /**
     * Feed all numbers through a processor until it returns a result.
     *
     * @param start to suppress all values < start.
     * @param process to generate a result to stop the processing.
     * @return R result if the processor or null if the stream run to its end.
     */
    <R> R forEach(long start, LongFunction<? extends R> process, long offset);

    default <R> R forEach(long start, LongFunction<? extends R> process) {
        return forEach(start, process, 0);
    }

    default <R> R forEach(LongFunction<? extends R> process, long offset) {
        return forEach(0, process, offset);
    }

    default <R> R forEach(LongFunction<? extends R> process) {
        return forEach(0, process, 0);
    }

    static <R> LongFunction<R> shift(LongFunction<R> process, long offset) {
        return offset==0 ? process : p -> process.apply(p+offset);
    }

    static <R> LongFunction<R> start(LongFunction<R> process, long start) {
        return start<0 ? process : p -> p<start ? null : process.apply(p);
    }

    /**
     * An empty sequence with nothing to process.
     */
    Sequence EMPTY = new Sequence() {
        @Override
        public <R> R forEach(long start, LongFunction<? extends R> process, long offset) {
            return null;
        }
    };
}