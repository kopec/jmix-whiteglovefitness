package com.company.whiteglovefitness.view;

import com.company.whiteglovefitness.component.FileRefResources;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.FileType;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.FileStorageLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Component
public class EquipmentFileCardRenderer {

    private final EquipmentViewFormatter equipmentViewFormatter;
    private final FileStorageLocator fileStorageLocator;

    @Autowired
    public EquipmentFileCardRenderer(EquipmentViewFormatter equipmentViewFormatter,
                                     FileStorageLocator fileStorageLocator) {
        this.equipmentViewFormatter = equipmentViewFormatter;
        this.fileStorageLocator = fileStorageLocator;
    }

    public List<EquipmentFile> filterFilesByType(Collection<EquipmentFile> files, FileType fileType) {
        if (files == null) {
            return List.of();
        }

        return files.stream()
                .filter(file -> fileType.equals(file.getFileType()))
                .toList();
    }

    public void renderFileCards(Div cards, List<EquipmentFile> files, FileType fileType, String emptyText,
                                Consumer<EquipmentFile> previewHandler) {
        List<EquipmentFile> cardFiles = files == null ? List.of() : files;

        cards.removeAll();
        if (cardFiles.isEmpty()) {
            cards.add(createEmptyMessage(emptyText));
            return;
        }

        cardFiles.forEach(file -> cards.add(createFileCard(file, fileType, previewHandler)));
    }

    private Div createFileCard(EquipmentFile file, FileType fileType, Consumer<EquipmentFile> previewHandler) {
        Div card = new Div();
        card.addClassName("reference-file-card");
        card.getElement().setAttribute("role", "button");
        card.getElement().setAttribute("tabindex", "0");
        card.getElement().setAttribute("title", equipmentViewFormatter.formatFileTitle(file));
        card.addClickListener(event -> previewHandler.accept(file));

        if (FileType.PHOTO.equals(fileType) && file.getFileRef() != null) {
            card.add(createPhotoThumbnail(file));
        } else {
            Icon icon = (FileType.VIDEO.equals(fileType) ? VaadinIcon.PLAY_CIRCLE : VaadinIcon.FILE).create();
            icon.addClassName("reference-file-card-icon");
            card.add(icon);
        }

        H5 title = new H5(equipmentViewFormatter.formatFileTitle(file));
        title.addClassName("reference-file-card-title");
        card.add(title);

        String description = file.getDescription();
        if (description != null && !description.isBlank()) {
            Span descriptionText = new Span(description);
            descriptionText.addClassName("reference-file-card-description");
            card.add(descriptionText);
        }

        return card;
    }

    private Div createPhotoThumbnail(EquipmentFile file) {
        Div thumbnail = new Div();
        thumbnail.addClassName("reference-file-card-media");
        thumbnail.getElement().setAttribute("role", "img");
        thumbnail.getElement().setAttribute("aria-label", equipmentViewFormatter.formatFileTitle(file));

        StreamRegistration registration = VaadinSession.getCurrent()
                .getResourceRegistry()
                .registerResource(
                        FileRefResources.inlineResource(file.getFileRef(), fileStorageLocator).allowDisabled(),
                        thumbnail.getElement());
        thumbnail.getStyle().set("background-image",
                "url(\"%s\")".formatted(registration.getResourceUri().toASCIIString()));
        thumbnail.addDetachListener(detachEvent -> registration.unregister());

        return thumbnail;
    }

    private Span createEmptyMessage(String emptyText) {
        Span emptyMessage = new Span(emptyText);
        emptyMessage.addClassName("reference-file-empty");
        return emptyMessage;
    }
}
