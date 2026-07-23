package com.company.whiteglovefitness.view.equipmentmodel;

import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


@Route(value = "equipment-models", layout = MainView.class)
@ViewController(id = "EquipmentModel.list")
@ViewDescriptor(path = "equipment-model-list-view.xml")
@LookupComponent("equipmentModelsDataGrid")
@DialogMode(width = "64em")
public class EquipmentModelListView extends StandardListView<EquipmentModel> {

    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    private DataGrid<EquipmentModel> equipmentModelsDataGrid;

    @ViewComponent
    private JmixButton referenceButton;

    @Subscribe
    public void onInit(final InitEvent event) {
        equipmentModelsDataGrid.addSelectionListener(selectionEvent -> updateReferenceButtonState());
        updateReferenceButtonState();
    }

    @Subscribe("referenceButton")
    public void onReferenceButtonClick(final ClickEvent<JmixButton> event) {
        EquipmentModel equipmentModel = equipmentModelsDataGrid.getSingleSelectedItem();
        if (equipmentModel == null) {
            return;
        }

        viewNavigators.detailView(this, EquipmentModel.class)
                .editEntity(equipmentModel)
                .withViewClass(EquipmentModelReferenceView.class)
                .withReadOnly(true)
                .navigate();
    }

    private void updateReferenceButtonState() {
        referenceButton.setEnabled(equipmentModelsDataGrid.getSingleSelectedItem() != null);
    }

}
