package de.ditz.array;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.05.25
 * Time: 17:49
 */
class DynamicIntArrayTest {

    @org.junit.jupiter.api.Test
    void testArray() {

        long i1 = (1L<<48) + (1L<<18) + 1001;
        long i2 = (1L<<43) + (1L<<17) + 1001;

        DynamicIntArray array = new DynamicIntArray();

        assertEquals(0, array.size());

        for(long i = 13; i<Long.MAX_VALUE/16; i *= 13) {
            array.setInt(i, (int)(i%64));
        }
        
        array.forEach(System.out::println);
    }
}