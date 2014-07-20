package com.freedomoss.ccompare;

import java.util.List;
import java.util.Optional;

public class Company {

    public final List<Morpheme> morphemes;
    public final Optional<BusinessEntity> businessEntity;

    public Company(List<Morpheme> morphemes, Optional<BusinessEntity> businessEntity) {
        this.morphemes = morphemes;
        this.businessEntity = businessEntity;
    }

    @Override
    public String toString() {
        return "Company{" +
                "morphemes=" + morphemes +
                ", businessEntity=" + businessEntity +
                '}';
    }
}
