package com.company.whiteglovefitness.equipmentmodel;

import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.EquipmentFileCardRenderer;
import com.company.whiteglovefitness.view.EquipmentViewFormatter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.Messages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class EquipmentFileCardRendererTest {

    @Test
    void filtersFilesByType() {
        EquipmentFileCardRenderer renderer = new EquipmentFileCardRenderer(
                new EquipmentViewFormatter(Mockito.mock(Messages.class)),
                Mockito.mock(FileStorageLocator.class));

        Assertions.assertEquals(List.of(), renderer.filterFilesByType(null, FileType.PHOTO));
        Assertions.assertEquals(1, renderer.filterFilesByType(List.of(
                file(FileType.PHOTO),
                file(FileType.VIDEO)), FileType.PHOTO).size());
    }

    @Test
    void videoCardsUseRatioBasedMediaBox() {
        EquipmentFileCardRenderer renderer = new EquipmentFileCardRenderer(
                new EquipmentViewFormatter(Mockito.mock(Messages.class)),
                Mockito.mock(FileStorageLocator.class));
        Div cards = new Div();

        renderer.renderFileCards(cards, List.of(file(FileType.VIDEO)), FileType.VIDEO, "No videos", file -> {
        });

        Assertions.assertEquals(1, cards.getComponentCount());
        Component card = cards.getComponentAt(0);
        Component media = card.getChildren().findFirst().orElseThrow();

        Assertions.assertTrue(media.getElement().getClassList().contains("reference-file-card-media"));
        Assertions.assertTrue(media.getElement().getClassList().contains("reference-file-card-icon-media"));
        Assertions.assertTrue(media.getElement().getClassList().contains("reference-file-card-video-media"));
    }

    private EquipmentFile file(FileType fileType) {
        EquipmentFile file = new EquipmentFile();
        file.setFileType(fileType);
        return file;
    }
}
