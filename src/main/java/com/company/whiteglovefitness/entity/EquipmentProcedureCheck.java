package com.company.whiteglovefitness.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.UUID;

@JmixEntity
@Table(name = "EQUIPMENT_PROCEDURE_CHECKLIST_ITEM", indexes = {
        @Index(name = "IDX_EQUIPMENT_PROCEDURE_CHECKLIST_ITEM_EQUIPMENT_PROCEDURE", columnList = "EQUIPMENT_PROCEDURE_ID")
})
@Entity
public class EquipmentProcedureCheck {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "EQUIPMENT_PROCEDURE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private EquipmentProcedure equipmentProcedure;

    @Column(name = "TEXT")
    private String statement;

    public EquipmentProcedure getEquipmentProcedure() {
        return equipmentProcedure;
    }

    public void setEquipmentProcedure(EquipmentProcedure equipmentProcedure) {
        this.equipmentProcedure = equipmentProcedure;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}