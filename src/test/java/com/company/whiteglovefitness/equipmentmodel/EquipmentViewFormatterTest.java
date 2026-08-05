package com.company.whiteglovefitness.equipmentmodel;

import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.entity.MeasurementUnit;
import com.company.whiteglovefitness.view.EquipmentViewFormatter;
import io.jmix.core.FileRef;
import io.jmix.core.Messages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

class EquipmentViewFormatterTest {

    private static final String MESSAGE_GROUP = "com.company.whiteglovefitness.view";

    @Test
    void formatsMeasurementValueAndUnitAbbreviations() {
        Messages messages = Mockito.mock(Messages.class);
        Mockito.when(messages.getMessage(MESSAGE_GROUP + ".equipmentmeasurement", "units.inch")).thenReturn("in");
        Mockito.when(messages.getMessage(MESSAGE_GROUP + ".equipmentmeasurement", "units.pound")).thenReturn("lb");
        EquipmentViewFormatter formatter = new EquipmentViewFormatter(messages);

        EquipmentMeasurement measurement = new EquipmentMeasurement();
        measurement.setValue(new BigDecimal("10.00"));
        measurement.setMeasurementUnit(MeasurementUnit.INCH);

        Assertions.assertEquals("10 in", formatter.formatMeasurementValueAndUnit(measurement));

        measurement.setMeasurementUnit(MeasurementUnit.POUND);

        Assertions.assertEquals("10 lb", formatter.formatMeasurementValueAndUnit(measurement));
    }

    @Test
    void formatsEstimatedMinutes() {
        Messages messages = Mockito.mock(Messages.class);
        Mockito.when(messages.getMessage(MESSAGE_GROUP + ".equipmentprocedure", "time.hours")).thenReturn("h");
        Mockito.when(messages.getMessage(MESSAGE_GROUP + ".equipmentprocedure", "time.minutes")).thenReturn("min");
        EquipmentViewFormatter formatter = new EquipmentViewFormatter(messages);

        Assertions.assertEquals("", formatter.formatEstimatedTime(null));
        Assertions.assertEquals("20 min", formatter.formatEstimatedTime(20));
        Assertions.assertEquals("1 h", formatter.formatEstimatedTime(60));
        Assertions.assertEquals("1 h 30 min", formatter.formatEstimatedTime(90));
    }

    @Test
    void formatsFileName() {
        EquipmentViewFormatter formatter = new EquipmentViewFormatter(Mockito.mock(Messages.class));

        Assertions.assertEquals("", formatter.formatFileName(null));
        Assertions.assertEquals("", formatter.formatFileName(FileRef.create("fs", "test/", null)));
        Assertions.assertEquals("setup-diagram.pdf",
                formatter.formatFileName(FileRef.create("fs", "test/setup-diagram.pdf", "setup-diagram.pdf")));
    }

    @Test
    void formatsFileTitle() {
        EquipmentViewFormatter formatter = new EquipmentViewFormatter(Mockito.mock(Messages.class));

        EquipmentFile titledFile = new EquipmentFile();
        titledFile.setTitle("Setup diagram");
        titledFile.setFileRef(FileRef.create("fs", "test/setup-diagram.pdf", "setup-diagram.pdf"));

        EquipmentFile untitledFile = new EquipmentFile();
        untitledFile.setFileRef(FileRef.create("fs", "test/model-photo.jpg", "model-photo.jpg"));

        Assertions.assertEquals("Setup diagram", formatter.formatFileTitle(titledFile));
        Assertions.assertEquals("model-photo.jpg", formatter.formatFileTitle(untitledFile));
    }

    @Test
    void formatsProcedureFileCounts() {
        Messages messages = Mockito.mock(Messages.class);
        Mockito.when(messages.getMessage(MESSAGE_GROUP + ".equipmentfile", "files.noFiles")).thenReturn("No files");
        Mockito.when(messages.getMessage(MESSAGE_GROUP + ".equipmentfile", "files.photo")).thenReturn("photo");
        Mockito.when(messages.getMessage(MESSAGE_GROUP + ".equipmentfile", "files.docs")).thenReturn("docs");
        EquipmentViewFormatter formatter = new EquipmentViewFormatter(messages);

        EquipmentProcedure procedure = new EquipmentProcedure();
        procedure.setEquipmentFiles(List.of(
                file(FileType.PHOTO),
                file(FileType.DOCUMENT),
                file(FileType.DOCUMENT)));

        Assertions.assertEquals("1 photo, 2 docs", formatter.formatProcedureFileCounts(procedure));
        Assertions.assertEquals("No files", formatter.formatProcedureFileCounts(new EquipmentProcedure()));
    }

    private EquipmentFile file(FileType fileType) {
        EquipmentFile file = new EquipmentFile();
        file.setFileType(fileType);
        return file;
    }
}
