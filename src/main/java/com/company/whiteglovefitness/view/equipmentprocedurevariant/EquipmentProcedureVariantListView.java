package com.company.whiteglovefitness.view.equipmentprocedurevariant;

import com.company.whiteglovefitness.entity.EquipmentProcedureType;
import com.company.whiteglovefitness.entity.EquipmentProcedureVariant;

import com.company.whiteglovefitness.view.main.MainView;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;


@Route(value = "equipment-procedure-variants", layout = MainView.class)
@ViewController(id = "EquipmentProcedureVariant.list")
@ViewDescriptor(path = "equipment-procedure-variant-list-view.xml")
@LookupComponent("equipmentProcedureVariantsDataGrid")
@DialogMode(width = "64em")
public class EquipmentProcedureVariantListView extends StandardListView<EquipmentProcedureVariant> {

    @ViewComponent
    private CollectionLoader<EquipmentProcedureVariant> equipmentProcedureVariantsDl;

    private EquipmentProcedureType equipmentProcedureTypeFilter;

    public void setEquipmentProcedureTypeFilter(EquipmentProcedureType equipmentProcedureTypeFilter) {
        this.equipmentProcedureTypeFilter = equipmentProcedureTypeFilter;
        if (equipmentProcedureVariantsDl != null) {
            applyEquipmentProcedureTypeFilter();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        applyEquipmentProcedureTypeFilter();
        super.beforeEnter(event);
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        if (equipmentProcedureTypeFilter != null) {
            applyEquipmentProcedureTypeFilter();
            equipmentProcedureVariantsDl.load();
        }
    }

    private void applyEquipmentProcedureTypeFilter() {
        if (equipmentProcedureTypeFilter == null) {
            equipmentProcedureVariantsDl.removeParameter("equipmentProcedureType");
            equipmentProcedureVariantsDl.setQuery("""
                    select e from EquipmentProcedureVariant e
                    order by e.name
                    """);
            return;
        }

        equipmentProcedureVariantsDl.setQuery("""
                select e from EquipmentProcedureVariant e
                where e.equipmentProcedureType = :equipmentProcedureType
                order by e.name
                """);
        equipmentProcedureVariantsDl.setParameter("equipmentProcedureType", equipmentProcedureTypeFilter.getId());
    }
}
