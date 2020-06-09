package de.ditz.primes;

import java.io.IOException;
import java.util.function.LongPredicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  09.06.2020 13:05
 * modified by: $
 * modified on: $
 */
public class BigList implements LongList {

    final ShortList prefix;

    final LongList tail;

    public BigList(ShortList prefix, LongList tail) {
        this.prefix = prefix;
        this.tail = tail;
    }

    @Override
    public int bits() {
        return tail.bits() + 24;
    }

    @Override
    public long size() {
        return tail.size();
    }


    @Override
    public int get(long index) {
        return 0;
    }

    @Override
    public boolean forEach(LongPredicate until) {
        return false;
    }

    @Override
    public void add(long value) {

    }

    @Override
    public void flush() {
        prefix.flush();
        tail.flush();
    }

    @Override
    public void close() throws IOException {
        prefix.close();
        tail.close();
    }
}
