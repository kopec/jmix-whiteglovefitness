package com.company.whiteglovefitness.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

public enum MeasurementUnit implements EnumClass<String> {

    INCH("INCH"),
    POUND("POUND");

    private final String id;

    MeasurementUnit(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static MeasurementUnit fromId(String id) {
        for (MeasurementUnit at : MeasurementUnit.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
