package com.company.whiteglovefitness.view.equipmentprocedure;

import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.EquipmentProcedureFile;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.core.FileRef;
import io.jmix.flowui.view.Supply;


@Route(value = "equipment-procedures/:id", layout = MainView.class)
@ViewController(id = "EquipmentProcedure.detail")
@ViewDescriptor(path = "equipment-procedure-detail-view.xml")
@EditedEntityContainer("equipmentProcedureDc")
public class EquipmentProcedureDetailView extends StandardDetailView<EquipmentProcedure> {
    @Supply(to = "equipmentProcedureFilesDataGrid.fileRef", subject = "renderer")
    private Renderer<EquipmentProcedureFile> attachmentsDataGridFileRenderer() {
        return new TextRenderer<>(equipmentProcedureFile -> {
            FileRef fileRef = equipmentProcedureFile.getFileRef();
            return fileRef != null ? fileRef.getFileName() : "";
        });
    }
}