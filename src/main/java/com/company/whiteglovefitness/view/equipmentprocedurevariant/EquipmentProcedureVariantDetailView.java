package com.company.whiteglovefitness.view.equipmentprocedurevariant;

import com.company.whiteglovefitness.entity.EquipmentProcedureVariant;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "equipment-procedure-variants/:id", layout = MainView.class)
@ViewController(id = "EquipmentProcedureVariant.detail")
@ViewDescriptor(path = "equipment-procedure-variant-detail-view.xml")
@EditedEntityContainer("equipmentProcedureVariantDc")
public class EquipmentProcedureVariantDetailView extends StandardDetailView<EquipmentProcedureVariant> {
}