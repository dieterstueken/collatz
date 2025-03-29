package de.ditz.draw;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaleTest {

    int len = 256;

    int len() {
        return len;
    }

    @Test
    public void testScale() {
        Scale scale = new Scale(this::len, "scale");

        assertEquals(len, scale.len());

        assertEquals(0.0, scale.lower());
        assertEquals(1.0, scale.upper());

        double x100 = 100.0/len;

        assertEquals(x100, scale.val(100));

        assertEquals(100, scale.pix(x100));

        scale.zoom(1.5, 100);

        double xl = scale.lower();

        assertTrue(xl!=0);



    }
}