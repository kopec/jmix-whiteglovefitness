package com.company.whiteglovefitness.equipmentprocedure;

import com.company.whiteglovefitness.WhiteGloveFitnessApplication;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.EquipmentProcedureCheck;
import com.company.whiteglovefitness.entity.EquipmentProcedureType;
import com.company.whiteglovefitness.entity.EquipmentProcedureVariant;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.equipmentprocedure.EquipmentProcedureDetailView;
import com.vaadin.flow.component.Component;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UiTest
@SpringBootTest(classes = {WhiteGloveFitnessApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
public class EquipmentProcedureDetailViewUiTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    private final List<Object> cleanup = new ArrayList<>();

    @Test
    void opensWithChecksAndFiles() {
        EquipmentModel model = saveModel();
        EquipmentProcedure procedure = saveProcedure(model);
        saveCheck(procedure);
        saveFile(procedure);

        viewNavigators.detailView(UiTestUtils.getCurrentView(), EquipmentProcedure.class)
                .editEntity(procedure)
                .withViewClass(EquipmentProcedureDetailView.class)
                .navigate();

        View<?> detailView = UiTestUtils.getCurrentView();

        EntityComboBox<EquipmentProcedureVariant> variantField =
                UiTestUtils.getComponent(detailView, "equipmentProcedureVariantField");
        Assertions.assertTrue(variantField.isVisible());
        Assertions.assertNotNull(variantField.getAction("entityLookupAction"));
        Assertions.assertNotNull(variantField.getAction("entityClearAction"));

        Component noteField = UiTestUtils.getComponent(detailView, "noteField");
        Assertions.assertTrue(noteField.isVisible());

        DataGrid<EquipmentProcedureCheck> checksDataGrid =
                UiTestUtils.getComponent(detailView, "equipmentProcedureChecksDataGrid");
        Assertions.assertTrue(checksDataGrid.isVisible());
        Assertions.assertTrue(items(checksDataGrid).stream()
                .anyMatch(check -> "Confirm feet are locked".equals(check.getStatement())));

        DataGrid<EquipmentFile> filesDataGrid =
                UiTestUtils.getComponent(detailView, "equipmentFilesDataGrid");
        Assertions.assertTrue(filesDataGrid.isVisible());
        Assertions.assertTrue(items(filesDataGrid).stream()
                .anyMatch(file -> "Setup diagram".equals(file.getTitle())
                        && FileType.DOCUMENT.equals(file.getFileType())));
    }

    @Test
    void filtersVariantOptionsByProcedureTypeAndClearsVariantWhenTypeChanges() {
        EquipmentModel model = saveModel();
        EquipmentProcedureVariant deliveryVariant =
                saveVariant("Ground level delivery", EquipmentProcedureType.CLIENT_SITE_DELIVERY);
        EquipmentProcedureVariant assemblyVariant =
                saveVariant("Warehouse assembly", EquipmentProcedureType.WAREHOUSE_ASSEMBLY);
        EquipmentProcedure procedure = saveProcedure(model, deliveryVariant);

        viewNavigators.detailView(UiTestUtils.getCurrentView(), EquipmentProcedure.class)
                .editEntity(procedure)
                .withViewClass(EquipmentProcedureDetailView.class)
                .navigate();

        View<?> detailView = UiTestUtils.getCurrentView();
        JmixSelect<EquipmentProcedureType> typeField =
                UiTestUtils.getComponent(detailView, "equipmentProcedureTypeField");
        EntityComboBox<EquipmentProcedureVariant> variantField =
                UiTestUtils.getComponent(detailView, "equipmentProcedureVariantField");

        Assertions.assertEquals(EquipmentProcedureType.CLIENT_SITE_DELIVERY, typeField.getValue());
        Assertions.assertTrue(variantItemsContain(variantField, deliveryVariant));
        Assertions.assertFalse(variantItemsContain(variantField, assemblyVariant));
        Assertions.assertEquals(deliveryVariant.getId(), variantField.getValue().getId());

        typeField.setValue(EquipmentProcedureType.WAREHOUSE_ASSEMBLY);

        Assertions.assertNull(variantField.getValue());
        Assertions.assertFalse(variantItemsContain(variantField, deliveryVariant));
        Assertions.assertTrue(variantItemsContain(variantField, assemblyVariant));
    }

    @AfterEach
    void tearDown() {
        for (int i = cleanup.size() - 1; i >= 0; i--) {
            dataManager.remove(cleanup.get(i));
        }
    }

    private EquipmentModel saveModel() {
        EquipmentModel model = dataManager.create(EquipmentModel.class);
        model.setName("Procedure detail model");
        model.setModelNumber("PRC-1000");
        model.setDescription("Procedure detail UI test model.");
        model.setRequiresCalibration(false);

        return save(model);
    }

    private EquipmentProcedure saveProcedure(EquipmentModel model) {
        return saveProcedure(model, saveVariant("Ground level delivery", EquipmentProcedureType.CLIENT_SITE_DELIVERY));
    }

    private EquipmentProcedure saveProcedure(EquipmentModel model, EquipmentProcedureVariant variant) {
        EquipmentProcedure procedure = dataManager.create(EquipmentProcedure.class);
        procedure.setEquipmentModel(model);
        procedure.setEquipmentProcedureType(variant.getEquipmentProcedureType());
        procedure.setEquipmentProcedureVariant(variant);
        procedure.setNote("Delivery setup");
        procedure.setEstimatedMinutes(90);
        procedure.setInstructions("Level the machine before final placement.");

        return save(procedure);
    }

    private EquipmentProcedureVariant saveVariant(String name, EquipmentProcedureType procedureType) {
        EquipmentProcedureVariant variant = dataManager.create(EquipmentProcedureVariant.class);
        variant.setName(name);
        variant.setEquipmentProcedureType(procedureType);

        return save(variant);
    }

    private void saveCheck(EquipmentProcedure procedure) {
        EquipmentProcedureCheck check = dataManager.create(EquipmentProcedureCheck.class);
        check.setEquipmentProcedure(procedure);
        check.setStatement("Confirm feet are locked");

        save(check);
    }

    private void saveFile(EquipmentProcedure procedure) {
        EquipmentFile file = dataManager.create(EquipmentFile.class);
        file.setEquipmentProcedure(procedure);
        file.setFileType(FileType.DOCUMENT);
        file.setTitle("Setup diagram");
        file.setDescription("Procedure setup diagram");
        file.setFileRef(FileRef.create(fileStorageLocator.getDefault().getStorageName(), "test/setup-diagram.pdf", "setup-diagram.pdf"));

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

    private List<EquipmentProcedureVariant> variantItems(EntityComboBox<EquipmentProcedureVariant> variantField) {
        return variantField.getGenericDataView().getItems().toList();
    }

    private boolean variantItemsContain(EntityComboBox<EquipmentProcedureVariant> variantField,
                                        EquipmentProcedureVariant variant) {
        return variantItems(variantField).stream()
                .anyMatch(item -> item.getId().equals(variant.getId()));
    }
}
