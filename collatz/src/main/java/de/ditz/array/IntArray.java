package de.ditz.array;

import java.util.function.IntConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.05.25
 * Time: 15:45
 */
public interface IntArray {

    long size();

    int getInt(long index);

    int setInt(long index, int value);

    void forEach(IntConsumer consumer);
}
