package com.company.whiteglovefitness.view.equipmentprocedure;

import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.EquipmentProcedureType;
import com.company.whiteglovefitness.entity.EquipmentProcedureVariant;
import com.company.whiteglovefitness.view.equipmentprocedurevariant.EquipmentProcedureVariantListView;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FileRef;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewDescriptor;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import io.jmix.flowui.view.Supply;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "equipment-procedures/:id", layout = MainView.class)
@ViewController(id = "EquipmentProcedure.detail")
@ViewDescriptor(path = "equipment-procedure-detail-view.xml")
@EditedEntityContainer("equipmentProcedureDc")
public class EquipmentProcedureDetailView extends StandardDetailView<EquipmentProcedure> {

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private JmixSelect<EquipmentProcedureType> equipmentProcedureTypeField;

    @ViewComponent
    private EntityComboBox<EquipmentProcedureVariant> equipmentProcedureVariantField;

    @ViewComponent
    private CollectionContainer<EquipmentProcedureVariant> equipmentProcedureVariantsDc;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        loadVariantOptions(getEditedEntity().getEquipmentProcedureType());
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        configureVariantLookupAction();
        equipmentProcedureTypeField.addValueChangeListener(valueChangeEvent -> {
            equipmentProcedureVariantField.clear();
            loadVariantOptions(valueChangeEvent.getValue());
        });
        equipmentProcedureVariantField.addValueChangeListener(valueChangeEvent -> {
            if (!isVariantAllowedForCurrentType(valueChangeEvent.getValue())) {
                equipmentProcedureVariantField.clear();
            }
        });
    }

    @Supply(to = "equipmentFilesDataGrid.fileRef", subject = "renderer")
    private Renderer<EquipmentFile> equipmentFilesDataGridFileRenderer() {
        return new TextRenderer<>(equipmentFile -> {
            FileRef fileRef = equipmentFile.getFileRef();
            return fileRef != null ? fileRef.getFileName() : "";
        });
    }

    private void configureVariantLookupAction() {
        if (equipmentProcedureVariantField.getAction("entityLookupAction") instanceof EntityLookupAction<?> lookupAction) {
            lookupAction.setViewClass(EquipmentProcedureVariantListView.class);
            lookupAction.setViewConfigurer((EquipmentProcedureVariantListView lookupView) ->
                    lookupView.setEquipmentProcedureTypeFilter(getEditedEntity().getEquipmentProcedureType()));
            lookupAction.setSelectValidator(validationContext ->
                    validationContext.getSelectedItems().stream()
                            .allMatch(item -> item instanceof EquipmentProcedureVariant variant
                                    && isVariantAllowedForCurrentType(variant)));
        }
    }

    private void loadVariantOptions(EquipmentProcedureType procedureType) {
        List<EquipmentProcedureVariant> variants = List.of();

        if (procedureType != null) {
            variants = dataManager.load(EquipmentProcedureVariant.class)
                    .query("""
                            select e from EquipmentProcedureVariant e
                            where e.equipmentProcedureType = :equipmentProcedureType
                            order by e.name
                            """)
                    .parameter("equipmentProcedureType", procedureType.getId())
                    .fetchPlan(FetchPlan.BASE)
                    .list();
        }

        equipmentProcedureVariantsDc.setItems(variants);
        equipmentProcedureVariantField.setAutoOpen(!variants.isEmpty());
    }

    private boolean isVariantAllowedForCurrentType(EquipmentProcedureVariant variant) {
        EquipmentProcedureType procedureType = getEditedEntity().getEquipmentProcedureType();
        return variant == null
                || procedureType != null && procedureType.equals(variant.getEquipmentProcedureType());
    }
}
