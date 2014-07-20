package com.freedomoss.ccompare;

import java.util.Optional;

public class BusinessEntityMatch {

    public enum Type {
        FULL, FEASIBLE, NONE
    }

    public final Optional<BusinessEntity> businessEntity1;
    public final Optional<BusinessEntity> businessEntity2;
    public final Type type;

    public BusinessEntityMatch(Optional<BusinessEntity> businessEntity1, Optional<BusinessEntity> businessEntity2, Type type) {
        this.businessEntity1 = businessEntity1;
        this.businessEntity2 = businessEntity2;
        this.type = type;
    }
}
