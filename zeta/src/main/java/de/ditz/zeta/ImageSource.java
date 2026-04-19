package de.ditz.zeta;

public interface ImageSource {
    int rgb(double x, double y);

    default View open(double scale) {
        return View.open(this, scale,512);
    }

    default View open(double scale, int size) {
        return View.open(this, scale, size);
    }

    default void hit(double x, double y) {

    }
}
