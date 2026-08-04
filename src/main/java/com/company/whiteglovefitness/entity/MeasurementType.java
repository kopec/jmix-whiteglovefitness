package com.company.whiteglovefitness.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

public enum MeasurementType implements EnumClass<String> {

    WIDTH("WIDTH"),
    LENGTH("LENGTH"),
    HEIGHT("HEIGHT"),
    DIAGONAL("DIAGONAL"),
    WEIGHT("WEIGHT");

    private final String id;

    MeasurementType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static MeasurementType fromId(String id) {
        for (MeasurementType at : MeasurementType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
