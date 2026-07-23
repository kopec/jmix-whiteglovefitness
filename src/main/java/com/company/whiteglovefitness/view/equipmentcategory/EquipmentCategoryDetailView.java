package com.company.whiteglovefitness.view.equipmentcategory;

import com.company.whiteglovefitness.entity.EquipmentCategory;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "equipment-categories/:id", layout = MainView.class)
@ViewController(id = "EquipmentCategory.detail")
@ViewDescriptor(path = "equipment-category-detail-view.xml")
@EditedEntityContainer("equipmentCategoryDc")
public class EquipmentCategoryDetailView extends StandardDetailView<EquipmentCategory> {
}