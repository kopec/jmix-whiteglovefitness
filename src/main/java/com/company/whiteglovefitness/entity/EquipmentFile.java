package com.company.whiteglovefitness.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.FileRef;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.AssertTrue;

import java.util.UUID;

@JmixEntity
@Table(name = "EQUIPMENT_FILE", indexes = {
        @Index(name = "IDX_EQUIPMENT_FILE_EQUIPMENT_MODEL", columnList = "EQUIPMENT_MODEL_ID"),
        @Index(name = "IDX_EQUIPMENT_FILE_EQUIPMENT_PROCEDURE", columnList = "EQUIPMENT_PROCEDURE_ID")
})
@Entity
public class EquipmentFile {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "FILE_TYPE")
    private String fileType;

    @Column(name = "FILE_REF", length = 1024)
    private FileRef fileRef;

    @InstanceName
    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION")
    @Lob
    private String description;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "EQUIPMENT_MODEL_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private EquipmentModel equipmentModel;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "EQUIPMENT_PROCEDURE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private EquipmentProcedure equipmentProcedure;

    public EquipmentModel getEquipmentModel() {
        return equipmentModel;
    }

    public void setEquipmentModel(EquipmentModel equipmentModel) {
        this.equipmentModel = equipmentModel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EquipmentProcedure getEquipmentProcedure() {
        return equipmentProcedure;
    }

    public void setEquipmentProcedure(EquipmentProcedure equipmentProcedure) {
        this.equipmentProcedure = equipmentProcedure;
    }

    public FileType getFileType() {
        return fileType == null ? null : FileType.fromId(fileType);
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType == null ? null : fileType.getId();
    }

    public FileRef getFileRef() {
        return fileRef;
    }

    public void setFileRef(FileRef fileRef) {
        this.fileRef = fileRef;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Transient
    @AssertTrue(message = "{msg://com.company.whiteglovefitness.entity/EquipmentFile.owner.required}")
    public boolean isOwnedByExactlyOneParent() {
        return (equipmentModel == null) != (equipmentProcedure == null);
    }
}
