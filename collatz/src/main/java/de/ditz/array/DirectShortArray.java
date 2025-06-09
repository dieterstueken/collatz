package de.ditz.array;

import java.util.function.IntConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.05.25
 * Time: 15:40
 */
public class DirectShortArray implements IntArray {

    final short[] values;

    public DirectShortArray(int size) {
        this.values = new short[size];
    }

    public long size() {
        return values.length;
    }

    public int getInt(long index) {
        return values[index(index)];
    }

    public int setInt(long index, int value) {
        short s = (short) value;
        if(s!=value)
            throw new IllegalArgumentException("value out of range: " + value);

        int i = index(index);
        int prev = values[i];
        values[i] = s;
        return prev;
    }

    private static int index(long index) {
        return index>Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) index;
    }

    public void forEach(IntConsumer consumer) {
        for (short value : values) {
            if(value!=0)
                consumer.accept(value);
        }

    }

}
