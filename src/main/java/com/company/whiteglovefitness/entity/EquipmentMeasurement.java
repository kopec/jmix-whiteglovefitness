package com.company.whiteglovefitness.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@JmixEntity
@Table(name = "EQUIPMENT_MEASUREMENT", indexes = {
        @Index(name = "IDX_EQUIPMENT_MEASUREMENT_EQUIPMENT_MODEL", columnList = "EQUIPMENT_MODEL_ID")
})
@Entity
public class EquipmentMeasurement {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "EQUIPMENT_MODEL_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private EquipmentModel equipmentModel;

    @Column(name = "MEASUREMENT_TYPE")
    private String measurementType;

    @InstanceName
    @Column(name = "LABEL")
    private String label;

    @Column(name = "VALUE_", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "MEASUREMENT_UNIT")
    private String measurementUnit;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MeasurementType getMeasurementType() {
        return measurementType == null ? null : MeasurementType.fromId(measurementType);
    }

    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType == null ? null : measurementType.getId();
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit == null ? null : MeasurementUnit.fromId(measurementUnit);
    }

    public void setMeasurementUnit(MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit == null ? null : measurementUnit.getId();
    }

    public EquipmentModel getEquipmentModel() {
        return equipmentModel;
    }

    public void setEquipmentModel(EquipmentModel equipmentModel) {
        this.equipmentModel = equipmentModel;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}