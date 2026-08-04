package com.company.whiteglovefitness.view.equipmentmodel;

import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.view.EquipmentViewFormatter;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "equipment-models/:id", layout = MainView.class)
@ViewController(id = "EquipmentModel.detail")
@ViewDescriptor(path = "equipment-model-detail-view.xml")
@EditedEntityContainer("equipmentModelDc")
public class EquipmentModelDetailView extends StandardDetailView<EquipmentModel> {

    @Autowired
    private EquipmentViewFormatter equipmentViewFormatter;

    @Supply(to = "equipmentFilesDataGrid.fileRef", subject = "renderer")
    private Renderer<EquipmentFile> equipmentFilesDataGridFileRenderer() {
        return new TextRenderer<>(equipmentFile -> {
            FileRef fileRef = equipmentFile.getFileRef();
            return fileRef != null ? fileRef.getFileName() : "";
        });
    }

    @Supply(to = "equipmentMeasurementsDataGrid.valueAndUnit", subject = "renderer")
    private Renderer<EquipmentMeasurement> equipmentMeasurementsDataGridValueAndUnitRenderer() {
        return new TextRenderer<>(equipmentViewFormatter::formatMeasurementValueAndUnit);
    }

    @Supply(to = "equipmentProceduresDataGrid.fileCounts", subject = "renderer")
    private Renderer<EquipmentProcedure> equipmentProceduresDataGridFileCountsRenderer() {
        return new TextRenderer<>(equipmentViewFormatter::formatProcedureFileCounts);
    }

    @Supply(to = "equipmentProceduresDataGrid.estimatedMinutes", subject = "renderer")
    private Renderer<EquipmentProcedure> equipmentProceduresDataGridEstimatedMinutesRenderer() {
        return new TextRenderer<>(procedure -> equipmentViewFormatter.formatEstimatedTime(procedure.getEstimatedMinutes()));
    }
}
