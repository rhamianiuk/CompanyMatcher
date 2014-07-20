package com.freedomoss.ccompare;

public class Morpheme {

    public final String payload;
    public final int position;

    public Morpheme(String payload, int position) {
        this.payload = payload;
        this.position = position;
    }

    @Override
    public String toString() {
        return "Morpheme{" +
                "payload='" + payload + '\'' +
                ", position=" + position +
                '}';
    }
}
