package com.company.whiteglovefitness.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

public enum EquipmentProcedureType implements EnumClass<String> {

    LOADING_PREPARATION("A"),
    VEHICLE_LOADING("B"),
    CLIENT_SITE_DELIVERY("C"),
    CLIENT_SITE_ASSEMBLY("D"),
    CALIBRATION("E"),
    TROUBLESHOOTING("F");

    private final String id;

    EquipmentProcedureType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static EquipmentProcedureType fromId(String id) {
        for (EquipmentProcedureType at : EquipmentProcedureType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}