package com.company.whiteglovefitness.view.equipmentmeasurement;

import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.entity.MeasurementType;
import com.company.whiteglovefitness.entity.MeasurementUnit;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "equipment-measurements/:id", layout = MainView.class)
@ViewController(id = "EquipmentMeasurement.detail")
@ViewDescriptor(path = "equipment-measurement-detail-view.xml")
@EditedEntityContainer("equipmentMeasurementDc")
public class EquipmentMeasurementDetailView extends StandardDetailView<EquipmentMeasurement> {

    @ViewComponent
    private JmixSelect<MeasurementType> measurementTypeField;

    @ViewComponent
    private JmixSelect<MeasurementUnit> measurementUnitField;

    @Subscribe
    public void onInit(final InitEvent event) {
        measurementTypeField.addValueChangeListener(valueChangeEvent ->
                selectDefaultUnit(valueChangeEvent.getValue(), true));
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        selectDefaultUnit(getEditedEntity().getMeasurementType(), false);
    }

    private void selectDefaultUnit(MeasurementType measurementType, boolean replaceExistingUnit) {
        MeasurementUnit defaultUnit = defaultUnitFor(measurementType);
        if (defaultUnit != null && (replaceExistingUnit || measurementUnitField.getValue() == null)) {
            measurementUnitField.setValue(defaultUnit);
        }
    }

    private MeasurementUnit defaultUnitFor(MeasurementType measurementType) {
        if (measurementType == null) {
            return null;
        }

        return switch (measurementType) {
            case WEIGHT -> MeasurementUnit.POUND;
            case WIDTH, LENGTH, HEIGHT, DIAGONAL -> MeasurementUnit.INCH;
        };
    }
}
