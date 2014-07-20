package com.freedomoss.ccompare;

public class BusinessEntity {

    public final String payload;

    public BusinessEntity(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return payload;
    }
}
