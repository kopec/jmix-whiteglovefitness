package com.company.whiteglovefitness.view.equipmentmodel;

import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.EquipmentProcedureFile;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.entity.MeasurementUnit;
import com.company.whiteglovefitness.view.equipmentprocedure.EquipmentProcedureReferenceView;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Route(value = "equipment-model-references/:id", layout = MainView.class)
@ViewController(id = "EquipmentModel.reference")
@ViewDescriptor(path = "equipment-model-reference-view.xml")
@EditedEntityContainer("equipmentModelDc")
public class EquipmentModelReferenceView extends StandardDetailView<EquipmentModel> {

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private Messages messages;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private DataGrid<EquipmentProcedure> equipmentProceduresDataGrid;

    @ViewComponent
    private JmixButton openProcedureButton;

    @Subscribe
    public void onInit(final InitEvent event) {
        equipmentProceduresDataGrid.addItemDoubleClickListener(
                doubleClickEvent -> openProcedureReference(doubleClickEvent.getItem()));
        equipmentProceduresDataGrid.addSelectionListener(
                selectionEvent -> updateOpenProcedureButtonState());
        updateOpenProcedureButtonState();
    }

    @Subscribe("openProcedureButton")
    public void onOpenProcedureButtonClick(final ClickEvent<JmixButton> event) {
        openProcedureReference(equipmentProceduresDataGrid.getSingleSelectedItem());
    }

    @Supply(to = "equipmentMeasurementsDataGrid.valueAndUnit", subject = "renderer")
    private Renderer<EquipmentMeasurement> equipmentMeasurementsDataGridValueAndUnitRenderer() {
        return new TextRenderer<>(this::formatMeasurementValueAndUnit);
    }

    @Supply(to = "equipmentProceduresDataGrid.fileCounts", subject = "renderer")
    private Renderer<EquipmentProcedure> equipmentProceduresDataGridFileCountsRenderer() {
        return new TextRenderer<>(this::formatProcedureFileCounts);
    }

    @Supply(to = "equipmentProceduresDataGrid.estimatedMinutes", subject = "renderer")
    private Renderer<EquipmentProcedure> equipmentProceduresDataGridEstimatedMinutesRenderer() {
        return new TextRenderer<>(procedure -> formatEstimatedTime(procedure.getEstimatedMinutes()));
    }

    private void updateOpenProcedureButtonState() {
        openProcedureButton.setEnabled(equipmentProceduresDataGrid.getSingleSelectedItem() != null);
    }

    private void openProcedureReference(EquipmentProcedure procedure) {
        if (procedure == null) {
            return;
        }

        dialogWindows.detail(this, EquipmentProcedure.class)
                .editEntity(procedure)
                .withViewClass(EquipmentProcedureReferenceView.class)
                .open();
    }

    private String formatProcedureFileCounts(EquipmentProcedure procedure) {
        List<String> fileCountParts = new ArrayList<>();
        addFileCount(fileCountParts, countFiles(procedure, FileType.PHOTO),
                "procedureFiles.photo", "procedureFiles.photos");
        addFileCount(fileCountParts, countFiles(procedure, FileType.VIDEO),
                "procedureFiles.video", "procedureFiles.videos");
        addFileCount(fileCountParts, countFiles(procedure, FileType.DOCUMENT),
                "procedureFiles.doc", "procedureFiles.docs");

        return fileCountParts.isEmpty()
                ? messageBundle.getMessage("procedureFiles.noFiles")
                : String.join(", ", fileCountParts);
    }

    private String formatMeasurementValueAndUnit(EquipmentMeasurement measurement) {
        BigDecimal value = measurement.getValue();
        MeasurementUnit unit = measurement.getMeasurementUnit();

        if (value == null) {
            return unit == null ? "" : messages.getMessage(unit);
        }

        String formattedValue = value.stripTrailingZeros().toPlainString();
        return unit == null ? formattedValue : formattedValue + " " + messages.getMessage(unit);
    }

    private String formatEstimatedTime(Integer estimatedMinutes) {
        if (estimatedMinutes == null) {
            return "";
        }

        int hours = estimatedMinutes / 60;
        int minutes = estimatedMinutes % 60;

        if (hours == 0) {
            return "%d %s".formatted(minutes, messageBundle.getMessage("estimatedTime.minutes"));
        }
        if (minutes == 0) {
            return "%d %s".formatted(hours, messageBundle.getMessage("estimatedTime.hours"));
        }

        return "%d %s %d %s".formatted(
                hours, messageBundle.getMessage("estimatedTime.hours"),
                minutes, messageBundle.getMessage("estimatedTime.minutes"));
    }

    private long countFiles(EquipmentProcedure procedure, FileType fileType) {
        List<EquipmentProcedureFile> files = procedure.getEquipmentProcedureFiles();
        if (files == null) {
            return 0;
        }

        return files.stream()
                .map(EquipmentProcedureFile::getFileType)
                .filter(fileType::equals)
                .count();
    }

    private void addFileCount(List<String> fileCountParts, long count, String singularMessageKey, String pluralMessageKey) {
        if (count == 0) {
            return;
        }

        String label = messageBundle.getMessage(count == 1 ? singularMessageKey : pluralMessageKey);
        fileCountParts.add("%d %s".formatted(count, label));
    }
}
