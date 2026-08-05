package com.company.whiteglovefitness.view;

import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.entity.MeasurementUnit;
import io.jmix.core.FileRef;
import io.jmix.core.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class EquipmentViewFormatter {

    private static final String MESSAGE_GROUP = "com.company.whiteglovefitness.view";

    private final Messages messages;

    @Autowired
    public EquipmentViewFormatter(Messages messages) {
        this.messages = messages;
    }

    public String formatProcedureFileCounts(EquipmentProcedure procedure) {
        List<String> fileCountParts = new ArrayList<>();
        addFileCount(fileCountParts, countFiles(procedure, FileType.PHOTO),
                "files.photo", "files.photos");
        addFileCount(fileCountParts, countFiles(procedure, FileType.VIDEO),
                "files.video", "files.videos");
        addFileCount(fileCountParts, countFiles(procedure, FileType.DOCUMENT),
                "files.doc", "files.docs");

        return fileCountParts.isEmpty()
                ? getMessage("equipmentfile", "files.noFiles")
                : String.join(", ", fileCountParts);
    }

    public String formatMeasurementValueAndUnit(EquipmentMeasurement measurement) {
        BigDecimal value = measurement.getValue();
        MeasurementUnit unit = measurement.getMeasurementUnit();

        if (value == null) {
            return unit == null ? "" : formatMeasurementUnit(unit);
        }

        String formattedValue = value.stripTrailingZeros().toPlainString();
        return unit == null ? formattedValue : formattedValue + " " + formatMeasurementUnit(unit);
    }

    public String formatEstimatedTime(Integer estimatedMinutes) {
        if (estimatedMinutes == null) {
            return "";
        }

        int hours = estimatedMinutes / 60;
        int minutes = estimatedMinutes % 60;

        if (hours == 0) {
            return "%d %s".formatted(minutes, getMessage("equipmentprocedure", "time.minutes"));
        }
        if (minutes == 0) {
            return "%d %s".formatted(hours, getMessage("equipmentprocedure", "time.hours"));
        }

        return "%d %s %d %s".formatted(
                hours, getMessage("equipmentprocedure", "time.hours"),
                minutes, getMessage("equipmentprocedure", "time.minutes"));
    }

    public String formatFileName(FileRef fileRef) {
        return fileRef == null || fileRef.getFileName() == null ? "" : fileRef.getFileName();
    }

    public String formatFileTitle(EquipmentFile file) {
        if (file == null) {
            return "";
        }
        if (file.getTitle() != null && !file.getTitle().isBlank()) {
            return file.getTitle();
        }

        return formatFileName(file.getFileRef());
    }

    private long countFiles(EquipmentProcedure procedure, FileType fileType) {
        List<EquipmentFile> files = procedure.getEquipmentFiles();
        if (files == null) {
            return 0;
        }

        return files.stream()
                .map(EquipmentFile::getFileType)
                .filter(fileType::equals)
                .count();
    }

    private void addFileCount(List<String> fileCountParts, long count, String singularMessageKey, String pluralMessageKey) {
        if (count == 0) {
            return;
        }

        String label = getMessage("equipmentfile", count == 1 ? singularMessageKey : pluralMessageKey);
        fileCountParts.add("%d %s".formatted(count, label));
    }

    private String formatMeasurementUnit(MeasurementUnit unit) {
        return switch (unit) {
            case INCH -> getMessage("equipmentmeasurement", "units.inch");
            case POUND -> getMessage("equipmentmeasurement", "units.pound");
        };
    }

    private String getMessage(String viewName, String key) {
        return messages.getMessage(MESSAGE_GROUP + "." + viewName, key);
    }
}
