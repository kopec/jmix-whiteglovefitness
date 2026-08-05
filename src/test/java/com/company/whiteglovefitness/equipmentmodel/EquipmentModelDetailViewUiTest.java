package com.company.whiteglovefitness.equipmentmodel;

import com.company.whiteglovefitness.WhiteGloveFitnessApplication;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.EquipmentProcedureType;
import com.company.whiteglovefitness.entity.EquipmentProcedureVariant;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.entity.MeasurementType;
import com.company.whiteglovefitness.entity.MeasurementUnit;
import com.company.whiteglovefitness.view.equipmentmodel.EquipmentModelDetailView;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UiTest
@SpringBootTest(classes = {WhiteGloveFitnessApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
public class EquipmentModelDetailViewUiTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    private final List<Object> cleanup = new ArrayList<>();

    @Test
    void opensWithFormattedMeasurementAndProcedureColumns() {
        EquipmentModel model = saveModel();
        saveModelFile(model);
        saveMeasurement(model);
        EquipmentProcedure procedure = saveProcedure(model, 90);
        saveFile(procedure);

        viewNavigators.detailView(UiTestUtils.getCurrentView(), EquipmentModel.class)
                .editEntity(model)
                .withViewClass(EquipmentModelDetailView.class)
                .navigate();

        View<?> detailView = UiTestUtils.getCurrentView();

        DataGrid<EquipmentFile> filesDataGrid =
                UiTestUtils.getComponent(detailView, "equipmentFilesDataGrid");
        Assertions.assertTrue(filesDataGrid.isVisible());
        Assertions.assertTrue(items(filesDataGrid).stream()
                .anyMatch(file -> "Owner manual".equals(file.getTitle())
                        && FileType.DOCUMENT.equals(file.getFileType())));

        DataGrid<EquipmentMeasurement> measurementsDataGrid =
                UiTestUtils.getComponent(detailView, "equipmentMeasurementsDataGrid");
        Assertions.assertTrue(measurementsDataGrid.isVisible());
        Assertions.assertNotNull(measurementsDataGrid.getColumnByKey("valueAndUnit"), measurementColumnKeys(measurementsDataGrid));
        Assertions.assertTrue(items(measurementsDataGrid).stream()
                .anyMatch(measurement -> "Overall height".equals(measurement.getNote())
                        && new BigDecimal("68.50").compareTo(measurement.getValue()) == 0
                        && MeasurementUnit.INCH.equals(measurement.getMeasurementUnit())));

        DataGrid<EquipmentProcedure> proceduresDataGrid =
                UiTestUtils.getComponent(detailView, "equipmentProceduresDataGrid");
        Assertions.assertTrue(proceduresDataGrid.isVisible());
        Assertions.assertNotNull(proceduresDataGrid.getColumnByKey("equipmentProcedureVariant"));
        Assertions.assertNotNull(proceduresDataGrid.getColumnByKey("note"));
        Assertions.assertNotNull(proceduresDataGrid.getColumnByKey("estimatedMinutes"));
        Assertions.assertNotNull(proceduresDataGrid.getColumnByKey("fileCounts"));

        EquipmentProcedure loadedProcedure = items(proceduresDataGrid).stream()
                .filter(item -> "Delivery setup".equals(item.getNote()))
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(3, loadedProcedure.getEquipmentFiles().size());
    }

    @AfterEach
    void tearDown() {
        for (int i = cleanup.size() - 1; i >= 0; i--) {
            dataManager.remove(cleanup.get(i));
        }
    }

    private EquipmentModel saveModel() {
        EquipmentModel model = dataManager.create(EquipmentModel.class);
        model.setName("Detail model");
        model.setModelNumber("DTL-1000");
        model.setDescription("Detail model for UI test.");
        model.setRequiresCalibration(false);

        return save(model);
    }

    private void saveMeasurement(EquipmentModel model) {
        EquipmentMeasurement measurement = dataManager.create(EquipmentMeasurement.class);
        measurement.setEquipmentModel(model);
        measurement.setMeasurementType(MeasurementType.HEIGHT);
        measurement.setNote("Overall height");
        measurement.setValue(new BigDecimal("68.50"));
        measurement.setMeasurementUnit(MeasurementUnit.INCH);

        save(measurement);
    }

    private void saveModelFile(EquipmentModel model) {
        EquipmentFile file = dataManager.create(EquipmentFile.class);
        file.setEquipmentModel(model);
        file.setFileType(FileType.DOCUMENT);
        file.setTitle("Owner manual");
        file.setDescription("Model owner manual");
        file.setFileRef(FileRef.create(fileStorageLocator.getDefault().getStorageName(), "test/owner-manual.pdf", "owner-manual.pdf"));

        save(file);
    }

    private EquipmentProcedure saveProcedure(EquipmentModel model, int estimatedMinutes) {
        EquipmentProcedure procedure = dataManager.create(EquipmentProcedure.class);
        procedure.setEquipmentModel(model);
        procedure.setEquipmentProcedureType(EquipmentProcedureType.CLIENT_SITE_DELIVERY);
        procedure.setEquipmentProcedureVariant(saveVariant());
        procedure.setNote("Delivery setup");
        procedure.setEstimatedMinutes(estimatedMinutes);
        procedure.setInstructions("Level the machine before final placement.");

        return save(procedure);
    }

    private EquipmentProcedureVariant saveVariant() {
        EquipmentProcedureVariant variant = dataManager.create(EquipmentProcedureVariant.class);
        variant.setName("Ground level delivery");
        variant.setEquipmentProcedureType(EquipmentProcedureType.CLIENT_SITE_DELIVERY);

        return save(variant);
    }

    private void saveFile(EquipmentProcedure procedure) {
        saveFile(procedure, FileType.PHOTO, "Setup photo", "setup-photo.jpg");
        saveFile(procedure, FileType.VIDEO, "Setup video", "setup-video.mp4");
        saveFile(procedure, FileType.DOCUMENT, "Setup diagram", "setup-diagram.pdf");
    }

    private void saveFile(EquipmentProcedure procedure, FileType fileType, String title, String fileName) {
        EquipmentFile file = dataManager.create(EquipmentFile.class);
        file.setEquipmentProcedure(procedure);
        file.setFileType(fileType);
        file.setTitle(title);
        file.setDescription("Detail setup diagram");
        file.setFileRef(FileRef.create(fileStorageLocator.getDefault().getStorageName(), "test/" + fileName, fileName));

        save(file);
    }

    private <T> T save(T entity) {
        T saved = dataManager.save(entity);
        cleanup.add(saved);
        return saved;
    }

    private <T> Collection<T> items(DataGrid<T> dataGrid) {
        DataGridItems<T> dataGridItems = dataGrid.getItems();
        Assertions.assertNotNull(dataGridItems);
        return dataGridItems.getItems();
    }

    private String measurementColumnKeys(DataGrid<EquipmentMeasurement> dataGrid) {
        return dataGrid.getColumns().stream()
                .map(column -> String.valueOf(column.getKey()))
                .toList()
                .toString();
    }
}
