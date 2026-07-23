package com.company.whiteglovefitness.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

public enum AccessCondition implements EnumClass<String> {

    GROUND_LEVEL("A"),
    ELEVATOR_ACCESS("B"),
    STAIRS_UP("C"),
    STAIRS_DOWN("D");

    private final String id;

    AccessCondition(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static AccessCondition fromId(String id) {
        for (AccessCondition at : AccessCondition.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}