package com.company.whiteglovefitness.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "EQUIPMENT_BRAND")
@Entity
public class EquipmentBrand {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Column(name = "WEBSITE")
    private String website;

    @OneToMany(mappedBy = "equipmentBrand")
    private List<EquipmentModel> equipmentModels;

    public List<EquipmentModel> getEquipmentModels() {
        return equipmentModels;
    }

    public void setEquipmentModels(List<EquipmentModel> equipmentModels) {
        this.equipmentModels = equipmentModels;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
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