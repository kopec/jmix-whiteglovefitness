package com.company.whiteglovefitness.view.equipmentmeasurement;

import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "equipment-measurements/:id", layout = MainView.class)
@ViewController(id = "EquipmentMeasurement.detail")
@ViewDescriptor(path = "equipment-measurement-detail-view.xml")
@EditedEntityContainer("equipmentMeasurementDc")
public class EquipmentMeasurementDetailView extends StandardDetailView<EquipmentMeasurement> {
}