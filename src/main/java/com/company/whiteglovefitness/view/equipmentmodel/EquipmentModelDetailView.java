package com.company.whiteglovefitness.view.equipmentmodel;

import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "equipment-models/:id", layout = MainView.class)
@ViewController(id = "EquipmentModel.detail")
@ViewDescriptor(path = "equipment-model-detail-view.xml")
@EditedEntityContainer("equipmentModelDc")
public class EquipmentModelDetailView extends StandardDetailView<EquipmentModel> {
}