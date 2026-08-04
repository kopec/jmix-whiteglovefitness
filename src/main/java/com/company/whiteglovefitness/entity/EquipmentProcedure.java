package com.company.whiteglovefitness.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "EQUIPMENT_PROCEDURE", indexes = {
        @Index(name = "IDX_EQUIPMENT_PROCEDURE_EQUIPMENT_MODEL", columnList = "EQUIPMENT_MODEL_ID"),
        @Index(name = "IDX_EQUIPMENT_PROCEDURE_EQUIPMENT_PROCEDURE_VARIANT", columnList = "EQUIPMENT_PROCEDURE_VARIANT_ID")
})
@Entity
public class EquipmentProcedure {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "EQUIPMENT_MODEL_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private EquipmentModel equipmentModel;

    @Column(name = "EQUIPMENT_PROCEDURE_TYPE")
    private String equipmentProcedureType;

    @JoinColumn(name = "EQUIPMENT_PROCEDURE_VARIANT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private EquipmentProcedureVariant equipmentProcedureVariant;

    @InstanceName
    @Column(name = "NOTE")
    private String note;

    @Column(name = "ESTIMATED_MINUTES")
    private Integer estimatedMinutes;

    @Column(name = "INSTRUCTIONS")
    @Lob
    private String instructions;

    @Composition
    @OneToMany(mappedBy = "equipmentProcedure")
    private List<EquipmentProcedureCheck> equipmentProcedureChecks;

    @Composition
    @OneToMany(mappedBy = "equipmentProcedure")
    private List<EquipmentFile> equipmentFiles;

    public EquipmentProcedureVariant getEquipmentProcedureVariant() {
        return equipmentProcedureVariant;
    }

    public void setEquipmentProcedureVariant(EquipmentProcedureVariant equipmentProcedureVariant) {
        this.equipmentProcedureVariant = equipmentProcedureVariant;
    }

    public EquipmentModel getEquipmentModel() {
        return equipmentModel;
    }

    public void setEquipmentModel(EquipmentModel equipmentModel) {
        this.equipmentModel = equipmentModel;
    }

    public List<EquipmentProcedureCheck> getEquipmentProcedureChecks() {
        return equipmentProcedureChecks;
    }

    public void setEquipmentProcedureChecks(List<EquipmentProcedureCheck> equipmentProcedureChecks) {
        this.equipmentProcedureChecks = equipmentProcedureChecks;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public EquipmentProcedureType getEquipmentProcedureType() {
        return equipmentProcedureType == null ? null : EquipmentProcedureType.fromId(equipmentProcedureType);
    }

    public void setEquipmentProcedureType(EquipmentProcedureType equipmentProcedureType) {
        this.equipmentProcedureType = equipmentProcedureType == null ? null : equipmentProcedureType.getId();
    }

    public List<EquipmentFile> getEquipmentFiles() {
        return equipmentFiles;
    }

    public void setEquipmentFiles(List<EquipmentFile> equipmentFiles) {
        this.equipmentFiles = equipmentFiles;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}
