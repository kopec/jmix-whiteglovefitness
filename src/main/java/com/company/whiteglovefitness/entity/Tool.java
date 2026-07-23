package com.company.whiteglovefitness.entity;

import io.jmix.core.FileRef;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "TOOL")
@Entity
public class Tool {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    @Lob
    private String description;

    @Column(name = "PHOTO", length = 1024)
    private FileRef photo;

    @JoinTable(name = "EQUIPMENT_PROCEDURE_TOOL_LINK",
            joinColumns = @JoinColumn(name = "TOOL_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "EQUIPMENT_PROCEDURE_ID", referencedColumnName = "ID"))
    @ManyToMany
    private List<EquipmentProcedure> equipmentProcedures;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FileRef getPhoto() {
        return photo;
    }

    public void setPhoto(FileRef photo) {
        this.photo = photo;
    }

    public List<EquipmentProcedure> getEquipmentProcedures() {
        return equipmentProcedures;
    }

    public void setEquipmentProcedures(List<EquipmentProcedure> equipmentProcedures) {
        this.equipmentProcedures = equipmentProcedures;
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

}