package com.company.whiteglovefitness.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "EQUIPMENT_MODEL", indexes = {
        @Index(name = "IDX_EQUIPMENT_MODEL_EQUIPMENT_BRAND", columnList = "EQUIPMENT_BRAND_ID"),
        @Index(name = "IDX_EQUIPMENT_MODEL_EQUIPMENT_CATEGORY", columnList = "EQUIPMENT_CATEGORY_ID")
})
@Entity
public class EquipmentModel {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "MODEL_NUMBER")
    private String modelNumber;

    @Column(name = "DESCRIPTION")
    @Lob
    private String description;

    @Column(name = "REQUIRES_CALIBRATION")
    private Boolean requiresCalibration;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @JoinColumn(name = "EQUIPMENT_BRAND_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private EquipmentBrand equipmentBrand;

    @OnDeleteInverse(DeletePolicy.UNLINK)
    @JoinColumn(name = "EQUIPMENT_CATEGORY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private EquipmentCategory equipmentCategory;

    @Composition
    @OneToMany(mappedBy = "equipmentModel")
    private List<EquipmentMeasurement> equipmentMeasurements;

    @Composition
    @OneToMany(mappedBy = "equipmentModel")
    private List<EquipmentFile> equipmentFiles;

    @Composition
    @OneToMany(mappedBy = "equipmentModel")
    private List<EquipmentProcedure> equipmentProcedures;

    public List<EquipmentFile> getEquipmentFiles() {
        return equipmentFiles;
    }

    public void setEquipmentFiles(List<EquipmentFile> equipmentFiles) {
        this.equipmentFiles = equipmentFiles;
    }

    public List<EquipmentProcedure> getEquipmentProcedures() {
        return equipmentProcedures;
    }

    public void setEquipmentProcedures(List<EquipmentProcedure> equipmentProcedures) {
        this.equipmentProcedures = equipmentProcedures;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public List<EquipmentMeasurement> getEquipmentMeasurements() {
        return equipmentMeasurements;
    }

    public void setEquipmentMeasurements(List<EquipmentMeasurement> equipmentMeasurements) {
        this.equipmentMeasurements = equipmentMeasurements;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequiresCalibration() {
        return requiresCalibration;
    }

    public void setRequiresCalibration(Boolean requiresCalibration) {
        this.requiresCalibration = requiresCalibration;
    }

    public EquipmentCategory getEquipmentCategory() {
        return equipmentCategory;
    }

    public void setEquipmentCategory(EquipmentCategory equipmentCategory) {
        this.equipmentCategory = equipmentCategory;
    }

    public EquipmentBrand getEquipmentBrand() {
        return equipmentBrand;
    }

    public void setEquipmentBrand(EquipmentBrand equipmentBrand) {
        this.equipmentBrand = equipmentBrand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"equipmentBrand", "name"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(equipmentBrand),
                metadataTools.format(name));
    }
}
