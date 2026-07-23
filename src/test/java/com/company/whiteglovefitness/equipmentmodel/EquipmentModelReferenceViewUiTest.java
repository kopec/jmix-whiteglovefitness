package com.company.whiteglovefitness.equipmentmodel;

import com.company.whiteglovefitness.WhiteGloveFitnessApplication;
import com.company.whiteglovefitness.entity.AccessCondition;
import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.EquipmentProcedureCheck;
import com.company.whiteglovefitness.entity.EquipmentProcedureFile;
import com.company.whiteglovefitness.entity.EquipmentProcedureType;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.entity.MeasurementType;
import com.company.whiteglovefitness.entity.MeasurementUnit;
import com.company.whiteglovefitness.view.equipmentprocedure.EquipmentProcedureReferenceView;
import com.company.whiteglovefitness.view.equipmentmodel.EquipmentModelListView;
import com.company.whiteglovefitness.view.equipmentmodel.EquipmentModelReferenceView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
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
public class EquipmentModelReferenceViewUiTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    private final List<Object> cleanup = new ArrayList<>();

    @Test
    void referenceButtonRequiresSelectedModel() {
        EquipmentModel model = saveModel();

        viewNavigators.view(UiTestUtils.getCurrentView(), EquipmentModelListView.class)
                .navigate();

        View<?> listView = UiTestUtils.getCurrentView();

        JmixButton referenceButton = UiTestUtils.getComponent(listView, "referenceButton");
        Assertions.assertFalse(referenceButton.isEnabled());

        DataGrid<EquipmentModel> modelsDataGrid =
                UiTestUtils.getComponent(listView, "equipmentModelsDataGrid");
        EquipmentModel loadedModel = items(modelsDataGrid).stream()
                .filter(item -> model.getId().equals(item.getId()))
                .findFirst()
                .orElseThrow();

        modelsDataGrid.select(loadedModel);

        Assertions.assertTrue(referenceButton.isEnabled());
    }

    @Test
    void opensReferenceViewWithModelMeasurementsAndProcedureLinks() {
        EquipmentModel model = saveModel();
        saveMeasurement(model);
        EquipmentProcedure procedure = saveProcedure(model, 90);
        saveFile(procedure);

        viewNavigators.detailView(UiTestUtils.getCurrentView(), EquipmentModel.class)
                .editEntity(model)
                .withViewClass(EquipmentModelReferenceView.class)
                .withReadOnly(true)
                .navigate();

        View<?> referenceView = UiTestUtils.getCurrentView();

        DataGrid<EquipmentMeasurement> measurementsDataGrid =
                UiTestUtils.getComponent(referenceView, "equipmentMeasurementsDataGrid");
        Assertions.assertTrue(measurementsDataGrid.isVisible());
        Assertions.assertTrue(measurementsDataGrid.isEnabled());
        Assertions.assertNotNull(measurementsDataGrid.getColumnByKey("valueAndUnit"));
        Assertions.assertTrue(items(measurementsDataGrid).stream()
                .anyMatch(measurement -> "Overall height".equals(measurement.getLabel())));

        DataGrid<EquipmentProcedure> proceduresDataGrid =
                UiTestUtils.getComponent(referenceView, "equipmentProceduresDataGrid");
        Assertions.assertTrue(proceduresDataGrid.isVisible());
        Assertions.assertTrue(proceduresDataGrid.isEnabled());
        EquipmentProcedure loadedProcedure = items(proceduresDataGrid).stream()
                .filter(item -> "Delivery setup".equals(item.getTitle()))
                .findFirst()
                .orElseThrow();
        Assertions.assertEquals(3, loadedProcedure.getEquipmentProcedureFiles().size());
        Assertions.assertTrue(loadedProcedure.getEquipmentProcedureFiles().stream()
                .anyMatch(file -> FileType.DOCUMENT.equals(file.getFileType())
                        && "Setup diagram".equals(file.getTitle())));

        JmixButton openProcedureButton = UiTestUtils.getComponent(referenceView, "openProcedureButton");
        proceduresDataGrid.select(loadedProcedure);
        openProcedureButton.click();

        Assertions.assertEquals(EquipmentModelReferenceView.class.getName(),
                UiTestUtils.getCurrentView().getClass().getName());
    }

    @Test
    void opensProcedureReferenceViewWithChecksAndFiles() {
        EquipmentModel model = saveModel();
        EquipmentProcedure procedure = saveProcedure(model, 90);
        saveCheck(procedure);
        saveFile(procedure);

        viewNavigators.detailView(UiTestUtils.getCurrentView(), EquipmentProcedure.class)
                .editEntity(procedure)
                .withViewClass(EquipmentProcedureReferenceView.class)
                .withReadOnly(true)
                .navigate();

        View<?> referenceView = UiTestUtils.getCurrentView();

        Markdown instructionsField = UiTestUtils.getComponent(referenceView, "instructionsField");
        Assertions.assertEquals("Level the machine before final placement.", instructionsField.getContent());

        TypedTextField<String> estimatedTimeField = UiTestUtils.getComponent(referenceView, "estimatedTimeField");
        Assertions.assertEquals("1 h 30 min", estimatedTimeField.getValue());

        JmixCheckboxGroup<EquipmentProcedureCheck> checksCheckboxGroup =
                UiTestUtils.getComponent(referenceView, "equipmentProcedureChecksCheckboxGroup");
        Assertions.assertFalse(checksCheckboxGroup.isReadOnly());

        Div photoCards = UiTestUtils.getComponent(referenceView, "photoCards");
        Assertions.assertEquals(1, photoCards.getComponentCount());

        Div videoCards = UiTestUtils.getComponent(referenceView, "videoCards");
        Assertions.assertEquals(1, videoCards.getComponentCount());

        DataGrid<EquipmentProcedureFile> documentFilesDataGrid =
                UiTestUtils.getComponent(referenceView, "documentFilesDataGrid");
        EquipmentProcedureFile loadedDocument = items(documentFilesDataGrid).stream()
                .filter(file -> "Setup diagram".equals(file.getTitle()))
                .findFirst()
                .orElseThrow();

        Button viewDocumentButton = UiTestUtils.getComponent(referenceView, "viewDocumentButton");
        Button downloadDocumentButton = UiTestUtils.getComponent(referenceView, "downloadDocumentButton");
        Assertions.assertFalse(viewDocumentButton.isEnabled());
        Assertions.assertFalse(downloadDocumentButton.isEnabled());

        documentFilesDataGrid.select(loadedDocument);

        Assertions.assertTrue(viewDocumentButton.isEnabled());
        Assertions.assertTrue(downloadDocumentButton.isEnabled());

        viewDocumentButton.click();
    }

    @AfterEach
    void tearDown() {
        for (int i = cleanup.size() - 1; i >= 0; i--) {
            dataManager.remove(cleanup.get(i));
        }
    }

    private EquipmentModel saveModel() {
        EquipmentModel model = dataManager.create(EquipmentModel.class);
        model.setName("Reference model");
        model.setModelNumber("REF-1000");
        model.setDescription("Reference model for field worker UI test.");
        model.setRequiresCalibration(false);

        return save(model);
    }

    private void saveMeasurement(EquipmentModel model) {
        EquipmentMeasurement measurement = dataManager.create(EquipmentMeasurement.class);
        measurement.setEquipmentModel(model);
        measurement.setMeasurementType(MeasurementType.HEIGHT);
        measurement.setLabel("Overall height");
        measurement.setValue(new BigDecimal("68.50"));
        measurement.setMeasurementUnit(MeasurementUnit.INCH);

        save(measurement);
    }

    private EquipmentProcedure saveProcedure(EquipmentModel model) {
        return saveProcedure(model, 20);
    }

    private EquipmentProcedure saveProcedure(EquipmentModel model, int estimatedMinutes) {
        EquipmentProcedure procedure = dataManager.create(EquipmentProcedure.class);
        procedure.setEquipmentModel(model);
        procedure.setEquipmentProcedureType(EquipmentProcedureType.CLIENT_SITE_DELIVERY);
        procedure.setAccessCondition(AccessCondition.GROUND_LEVEL);
        procedure.setTitle("Delivery setup");
        procedure.setEstimatedMinutes(estimatedMinutes);
        procedure.setInstructions("Level the machine before final placement.");

        return save(procedure);
    }

    private void saveCheck(EquipmentProcedure procedure) {
        EquipmentProcedureCheck check = dataManager.create(EquipmentProcedureCheck.class);
        check.setEquipmentProcedure(procedure);
        check.setStatement("Confirm feet are locked");

        save(check);
    }

    private void saveFile(EquipmentProcedure procedure) {
        saveFile(procedure, FileType.PHOTO, "Setup photo", "setup-photo.jpg");
        saveFile(procedure, FileType.VIDEO, "Setup video", "setup-video.mp4");
        saveFile(procedure, FileType.DOCUMENT, "Setup diagram", "setup-diagram.pdf");
    }

    private void saveFile(EquipmentProcedure procedure, FileType fileType, String title, String fileName) {
        EquipmentProcedureFile file = dataManager.create(EquipmentProcedureFile.class);
        file.setEquipmentProcedure(procedure);
        file.setFileType(fileType);
        file.setTitle(title);
        file.setDescription("Reference setup diagram");
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

}
