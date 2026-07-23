package com.company.whiteglovefitness.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

public enum MeasurementType implements EnumClass<String> {

    WIDTH("A"),
    LENGTH("B"),
    HEIGHT("C"),
    DIAGONAL("D"),
    WEIGHT("E");

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