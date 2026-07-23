package com.company.whiteglovefitness.view.equipmentbrand;

import com.company.whiteglovefitness.entity.EquipmentBrand;

import com.company.whiteglovefitness.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "equipment-brands", layout = MainView.class)
@ViewController(id = "EquipmentBrand.list")
@ViewDescriptor(path = "equipment-brand-list-view.xml")
@LookupComponent("equipmentBrandsDataGrid")
@DialogMode(width = "64em")
public class EquipmentBrandListView extends StandardListView<EquipmentBrand> {

}