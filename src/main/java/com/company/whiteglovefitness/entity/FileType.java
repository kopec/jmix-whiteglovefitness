package com.company.whiteglovefitness.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

public enum FileType implements EnumClass<String> {

    PHOTO("A"),
    VIDEO("B"),
    DOCUMENT("C");

    private final String id;

    FileType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static FileType fromId(String id) {
        for (FileType at : FileType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}