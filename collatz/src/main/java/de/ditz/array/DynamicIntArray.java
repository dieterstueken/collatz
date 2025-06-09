package de.ditz.array;

import java.util.function.IntConsumer;

import static de.ditz.array.IndirectIntArray.allocate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.05.25
 * Time: 17:17
 */

public class DynamicIntArray implements IntArray {

    IntArray array;

    DynamicIntArray(int base) {
        array = allocate(base);
    }

    DynamicIntArray() {
    }

    @Override
    public long size() {
        return array==null ? 0 : array.size();
    }

    @Override
    public int getInt(long index) {
        return index<size() ? array.getInt(index) : 0;
    }

    @Override
    public int setInt(long index, int value) {
        IntArray array = this.array;
        if(array == null || index>=array.size()) {
            // no need to set up new array
            if(value==0)
                return 0;

            array = resizeTo(index + 1);
        }

        return array.setInt(index, value);
    }

    @Override
    public void forEach(IntConsumer consumer) {
        if(array!=null)
            array.forEach(consumer);
    }

    private synchronized IntArray resizeTo(long index) {
        IntArray array = this.array;
        if(array == null) {
            this.array = array = allocate(index);
        } else
        if(array.size() <= index) {
            do {
                array = new IndirectIntArray(array);
            } while (array.size() <= index);
            this.array = array;
        }
        return array;
    }
}
