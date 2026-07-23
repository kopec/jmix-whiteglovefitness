package com.company.whiteglovefitness.view.equipmentcategory;

import com.company.whiteglovefitness.entity.EquipmentCategory;

import com.company.whiteglovefitness.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "equipment-categories", layout = MainView.class)
@ViewController(id = "EquipmentCategory.list")
@ViewDescriptor(path = "equipment-category-list-view.xml")
@LookupComponent("equipmentCategoriesDataGrid")
@DialogMode(width = "64em")
public class EquipmentCategoryListView extends StandardListView<EquipmentCategory> {

}