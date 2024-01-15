package de.ditz.primes;

import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.06.20
 * Time: 20:42
 */
public interface Sequence {

    /**
     * Feed all numbers through a test until it fails.
     *
     * @param start
     * @param until test to stop processing.
     * @return true if the stream was ended by until.test or was aborted otherwise.
     */
    boolean forEach(long start, LongPredicate until, long offset);

    default boolean forEach(long start, LongPredicate until) {
        return forEach(start, until, 0);
    }

    default boolean forEach(LongPredicate until, long offset) {
        return forEach(0, until, offset);
    }

    default boolean forEach(LongPredicate until) {
        return forEach(0, until, 0);
    }

    static LongPredicate shift(LongPredicate until, long offset) {
        return offset==0 ? until : p -> until.test(p+offset);
    }

    static LongPredicate start(LongPredicate until, long start) {
        return start<0 ? until : p -> p>=start && until.test(p);
    }

    Sequence EMPTY = (start, until, offset) -> false;
}

