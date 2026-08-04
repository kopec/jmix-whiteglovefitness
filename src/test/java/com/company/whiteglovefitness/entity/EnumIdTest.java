package com.company.whiteglovefitness.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnumIdTest {

    @Test
    void enumIdsUseStableMeaningfulCodes() {
        assertIdsMatchNames(EquipmentProcedureType.values());
        assertIdsMatchNames(FileType.values());
        assertIdsMatchNames(MeasurementType.values());
        assertIdsMatchNames(MeasurementUnit.values());
    }

    private void assertIdsMatchNames(Enum<?>[] values) {
        for (Enum<?> value : values) {
            Assertions.assertEquals(value.name(), ((EnumClass<?>) value).getId());
        }
    }
}
