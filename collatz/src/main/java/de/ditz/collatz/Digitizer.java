package de.ditz.collatz;

import java.math.BigInteger;

public class Digitizer {

    final String digits;
    final boolean revert ;

    final BigInteger base;

    final StringBuilder result = new StringBuilder();

    public Digitizer(String digits, boolean revert) {
        this.digits = digits;
        this.base = BigInteger.valueOf(digits.length());
        this.revert = revert;
    }

    public Digitizer(String digits) {
        this(digits, false);
    }

    public StringBuilder digits(BigInteger n) {
        return digits(n, 1024);
    }

    public StringBuilder digits(BigInteger n, int limit) {
        result.setLength(0);

        //if(n.signum()==0)
        //    result.append('0');

        while(n.signum() > 0 && result.length() < limit) {
            int digit = n.remainder(base).intValue();

            if(revert)
                result.append(digits.charAt(digit));
            else
                result.insert(0, digits.charAt(digit));
            
            n = n.divide(base);
        }

        return result;
    }
}
