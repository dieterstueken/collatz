package de.ditz.collatz;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.03.25
 * Time: 18:55
 */
public class Pow23 {

    final List<BigInteger> pow3;

    Pow23(int len) {
        pow3 = new ArrayList<>(len);

        BigInteger i3 = BigInteger.valueOf(3);
        for(BigInteger p3 = BigInteger.ONE; pow3.size()<len; p3 = p3.multiply(i3)) {
            pow3.add(p3);
        }
    }

    void show() {
        int max = pow3.getLast().bitLength();

        for(int l=0; l<max; ++l) {
            System.out.format("%2d ", l);

            for (int i = 0; i < pow3.size(); i++) {
                BigInteger p3 = pow3.get(i);
                boolean set = p3.testBit(l);
                System.out.append(set ? '1' : '.');
                if(i%8==7)
                    System.out.append(' ');
            }

            System.out.println();
        }
    }

    public static void main(String ... args) {
        new Pow23(64).show();
    }
}
