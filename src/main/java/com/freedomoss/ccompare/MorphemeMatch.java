package com.freedomoss.ccompare;

public class MorphemeMatch {

    public enum Type {
        FULL(4), LEVENSHTAIN(3), SHORTENING(2), SYNONYM(1), NONE(0);
        public final int weight;
        Type(int weight) {
            this.weight = weight;
        }
    }

    public final Morpheme morpheme1;
    public final Morpheme morpheme2;
    public final Type type;
    public final int score;

    public MorphemeMatch(Morpheme morpheme1, Morpheme morpheme2, Type type, int score) {
        this.morpheme1 = morpheme1;
        this.morpheme2 = morpheme2;
        this.type = type;
        this.score = score;
    }

    @Override
    public String toString() {
        return "MorphemeMatch{" +
                "morpheme1=" + morpheme1.payload +
                ", morpheme2=" + morpheme2.payload +
                ", type=" + type +
                ", score=" + score +
                '}';
    }
}
