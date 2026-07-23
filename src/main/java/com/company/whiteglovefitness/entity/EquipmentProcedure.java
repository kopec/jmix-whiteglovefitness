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
        @Index(name = "IDX_EQUIPMENT_PROCEDURE_EQUIPMENT_MODEL", columnList = "EQUIPMENT_MODEL_ID")
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

    @Column(name = "ACCESS_CONDITION")
    private String accessCondition;

    @InstanceName
    @Column(name = "LABEL")
    private String title;

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
    private List<EquipmentProcedureFile> equipmentProcedureFiles;

    @JoinTable(name = "EQUIPMENT_PROCEDURE_TOOL_LINK",
            joinColumns = @JoinColumn(name = "EQUIPMENT_PROCEDURE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "TOOL_ID", referencedColumnName = "ID"))
    @ManyToMany
    private List<Tool> tools;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools = tools;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition == null ? null : AccessCondition.fromId(accessCondition);
    }

    public void setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition == null ? null : accessCondition.getId();
    }

    public EquipmentProcedureType getEquipmentProcedureType() {
        return equipmentProcedureType == null ? null : EquipmentProcedureType.fromId(equipmentProcedureType);
    }

    public void setEquipmentProcedureType(EquipmentProcedureType equipmentProcedureType) {
        this.equipmentProcedureType = equipmentProcedureType == null ? null : equipmentProcedureType.getId();
    }

    public List<EquipmentProcedureFile> getEquipmentProcedureFiles() {
        return equipmentProcedureFiles;
    }

    public void setEquipmentProcedureFiles(List<EquipmentProcedureFile> equipmentProcedureFiles) {
        this.equipmentProcedureFiles = equipmentProcedureFiles;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}