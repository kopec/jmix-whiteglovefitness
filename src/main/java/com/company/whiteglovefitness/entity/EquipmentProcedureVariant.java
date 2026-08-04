package com.company.whiteglovefitness.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "EQUIPMENT_PROCEDURE_VARIANT")
@Entity
public class EquipmentProcedureVariant {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "EQUIPMENT_PROCEDURE_TYPE")
    private String equipmentProcedureType;

    @OneToMany(mappedBy = "equipmentProcedureVariant")
    private List<EquipmentProcedure> equipmentProcedures;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EquipmentProcedureType getEquipmentProcedureType() {
        return equipmentProcedureType == null ? null : EquipmentProcedureType.fromId(equipmentProcedureType);
    }

    public void setEquipmentProcedureType(EquipmentProcedureType equipmentProcedureType) {
        this.equipmentProcedureType = equipmentProcedureType == null ? null : equipmentProcedureType.getId();
    }

    public List<EquipmentProcedure> getEquipmentProcedures() {
        return equipmentProcedures;
    }

    public void setEquipmentProcedures(List<EquipmentProcedure> equipmentProcedures) {
        this.equipmentProcedures = equipmentProcedures;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}