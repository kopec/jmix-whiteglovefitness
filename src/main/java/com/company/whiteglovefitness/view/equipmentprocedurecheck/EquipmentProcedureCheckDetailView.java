package com.company.whiteglovefitness.view.equipmentprocedurecheck;

import com.company.whiteglovefitness.entity.EquipmentProcedureCheck;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "equipment-procedure-checks/:id", layout = MainView.class)
@ViewController(id = "EquipmentProcedureCheck.detail")
@ViewDescriptor(path = "equipment-procedure-check-detail-view.xml")
@EditedEntityContainer("equipmentProcedureCheckDc")
public class EquipmentProcedureCheckDetailView extends StandardDetailView<EquipmentProcedureCheck> {
}