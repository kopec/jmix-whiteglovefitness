package com.company.whiteglovefitness.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.FileRef;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.UUID;

@JmixEntity
@Table(name = "EQUIPMENT_PROCEDURE_FILE", indexes = {
        @Index(name = "IDX_EQUIPMENT_PROCEDURE_FILE_EQUIPMENT_PROCEDURE", columnList = "EQUIPMENT_PROCEDURE_ID")
})
@Entity
public class EquipmentProcedureFile {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "FILE_TYPE")
    private String fileType;

    @Column(name = "FILE_REF", length = 1024)
    private FileRef fileRef;

    @Column(name = "TITLE")
    private String title;

    @InstanceName
    @Column(name = "DESCRIPTION")
    @Lob
    private String description;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "EQUIPMENT_PROCEDURE_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private EquipmentProcedure equipmentProcedure;

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

}