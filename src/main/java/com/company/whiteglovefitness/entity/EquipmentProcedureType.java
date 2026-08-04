package com.company.whiteglovefitness.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

public enum EquipmentProcedureType implements EnumClass<String> {

    LOADING_PREPARATION("LOADING_PREPARATION"),
    WAREHOUSE_ASSEMBLY("WAREHOUSE_ASSEMBLY"),
    CLIENT_SITE_DELIVERY("CLIENT_SITE_DELIVERY"),
    CLIENT_SITE_ASSEMBLY("CLIENT_SITE_ASSEMBLY"),
    CALIBRATION("CALIBRATION"),
    TROUBLESHOOTING("TROUBLESHOOTING");

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
