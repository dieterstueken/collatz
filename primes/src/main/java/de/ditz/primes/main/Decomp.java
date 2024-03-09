package de.ditz.primes.main;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.03.24
 * Time: 18:07
 */
public class Decomp {

    /*
     * Computes the discrete Fourier transform (DFT) of the given complex vector.
     * All the array arguments must be non-null and have the same length.
     */
    public static void computeDft(double[] inreal, double[] inimag, double[] outreal, double[] outimag) {
        int n = inreal.length;
        for (int k = 0; k < n; k++) {  // For each output element
            double sumreal = 0;
            double sumimag = 0;
            for (int t = 0; t < n; t++) {  // For each input element
                double angle = 2 * Math.PI * t * k / n;
                sumreal += inreal[t] * Math.cos(angle) + inimag[t] * Math.sin(angle);
                sumimag += -inreal[t] * Math.sin(angle) + inimag[t] * Math.cos(angle);
            }
            outreal[k] = sumreal;
            outimag[k] = sumimag;
        }
    }

    public static void main(String ... args) {
        int LEN = 128;

        double[] inr = new double[LEN];
        double[] ini = new double[LEN];
        double[] outr = new double[LEN];
        double[] outi = new double[LEN];

        inr[0] = 1;
        inr[LEN/2] = -1;

        computeDft(inr, ini, outr, outi);

        for(int i=0; i<LEN; i+=1) {
            System.out.format("%5.1f\n",outr[i]);
        }
    }

}
