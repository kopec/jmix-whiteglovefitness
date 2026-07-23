package com.company.whiteglovefitness.view.equipmentbrand;

import com.company.whiteglovefitness.entity.EquipmentBrand;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "equipment-brands/:id", layout = MainView.class)
@ViewController(id = "EquipmentBrand.detail")
@ViewDescriptor(path = "equipment-brand-detail-view.xml")
@EditedEntityContainer("equipmentBrandDc")
public class EquipmentBrandDetailView extends StandardDetailView<EquipmentBrand> {
}