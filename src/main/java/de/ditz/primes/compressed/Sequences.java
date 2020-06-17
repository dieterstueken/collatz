package de.ditz.primes.compressed;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.06.20
 * Time: 21:58
 */
interface Sequences {

    byte[] base = new byte[]{1,7,11,13,17,19,23,29};

    static Sequence compact(int index) {
        if(index>255)
            throw new IndexOutOfBoundsException();

        byte[] seq = new byte[Integer.bitCount(index)];
        int n = 0;
        for(int i=0; i<8; ++i) {
            int m = 1<<8;
            if((index&m)!=0)
                seq[n++] = base[i];
        }

        return sequence(seq);
    }

    static Sequence sequence(byte[] seq) {
        return (seek, until) -> {
            for (byte b : seq) {
                if(b>=seek && until.test(0xff & b))
                    return true;
            }
            return false;
        };
    }

}
