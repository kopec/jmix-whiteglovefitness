package com.company.whiteglovefitness.view.equipmentprocedure;

import com.company.whiteglovefitness.component.FileRefResources;
import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.EquipmentProcedureCheck;
import com.company.whiteglovefitness.entity.EquipmentProcedureFile;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.equipmentprocedure.EquipmentProcedureFilePreviewView.PreviewMode;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Route(value = "equipment-procedure-references/:id", layout = MainView.class)
@ViewController(id = "EquipmentProcedure.reference")
@ViewDescriptor(path = "equipment-procedure-reference-view.xml")
@EditedEntityContainer("equipmentProcedureDc")
public class EquipmentProcedureReferenceView extends StandardDetailView<EquipmentProcedure> {

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private TypedTextField<String> estimatedTimeField;

    @ViewComponent
    private H4 equipmentModelNameHeading;

    @ViewComponent
    private Markdown instructionsField;

    @ViewComponent
    private CollectionContainer<EquipmentProcedureCheck> equipmentProcedureChecksDc;

    @ViewComponent
    private JmixCheckboxGroup<EquipmentProcedureCheck> equipmentProcedureChecksCheckboxGroup;

    @ViewComponent
    private CollectionContainer<EquipmentProcedureFile> equipmentProcedureFilesDc;

    @ViewComponent
    private CollectionContainer<EquipmentProcedureFile> documentFilesDc;

    @ViewComponent
    private Div photoCards;

    @ViewComponent
    private Div videoCards;

    @ViewComponent
    private HorizontalLayout documentFilesButtonsPanel;

    @ViewComponent
    private DataGrid<EquipmentProcedureFile> documentFilesDataGrid;

    private Button viewDocumentButton;
    private Button downloadDocumentButton;
    private Anchor downloadDocumentAnchor;

    private Set<EquipmentProcedureCheck> checkedProcedureChecks = new LinkedHashSet<>();

    @Subscribe
    public void onInit(final InitEvent event) {
        initDocumentButtons();
        documentFilesDataGrid.addSelectionListener(selectionEvent -> updateDocumentButtons());
        documentFilesDataGrid.addItemDoubleClickListener(doubleClickEvent -> openDocumentPreview(doubleClickEvent.getItem()));
        documentFilesDataGrid.setEmptyStateText(messageBundle.getMessage("documentsEmpty.text"));
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        equipmentModelNameHeading.setText(formatEquipmentModelName(getEditedEntity().getEquipmentModel()));
        estimatedTimeField.setValue(formatEstimatedTime(getEditedEntity().getEstimatedMinutes()));
        instructionsField.setContent(formatInstructions(getEditedEntity().getInstructions()));
        equipmentProcedureChecksCheckboxGroup.setItemLabelGenerator(this::formatChecklistStatement);
        equipmentProcedureChecksCheckboxGroup.setValue(checkedProcedureChecks);
        populateFileSections();
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        equipmentProcedureChecksCheckboxGroup.addValueChangeListener(valueChangeEvent ->
                checkedProcedureChecks = new LinkedHashSet<>(valueChangeEvent.getValue()));
    }

    @Supply(to = "documentFilesDataGrid.fileRef", subject = "renderer")
    private Renderer<EquipmentProcedureFile> documentFilesDataGridFileRenderer() {
        return new TextRenderer<>(equipmentProcedureFile -> {
            FileRef fileRef = equipmentProcedureFile.getFileRef();
            return formatFileName(fileRef);
        });
    }

    private void initDocumentButtons() {
        viewDocumentButton = new Button(messageBundle.getMessage("viewDocumentButton.text"),
                VaadinIcon.EYE.create(),
                event -> openDocumentPreview(documentFilesDataGrid.getSingleSelectedItem()));
        viewDocumentButton.setId("viewDocumentButton");
        viewDocumentButton.getElement().setAttribute("title", messageBundle.getMessage("viewDocumentButton.text"));

        downloadDocumentButton = new Button(messageBundle.getMessage("downloadDocumentButton.text"),
                VaadinIcon.DOWNLOAD.create());
        downloadDocumentButton.setId("downloadDocumentButton");
        downloadDocumentButton.getElement().setAttribute("title", messageBundle.getMessage("downloadDocumentButton.text"));

        downloadDocumentAnchor = new Anchor();
        downloadDocumentAnchor.setId("downloadDocumentAnchor");
        downloadDocumentAnchor.setDownload(true);
        downloadDocumentAnchor.add(downloadDocumentButton);

        documentFilesButtonsPanel.add(viewDocumentButton, downloadDocumentAnchor);
        updateDocumentButtons();
    }

    private void populateFileSections() {
        List<EquipmentProcedureFile> photos = equipmentProcedureFilesDc.getItems().stream()
                .filter(file -> FileType.PHOTO.equals(file.getFileType()))
                .toList();
        List<EquipmentProcedureFile> videos = equipmentProcedureFilesDc.getItems().stream()
                .filter(file -> FileType.VIDEO.equals(file.getFileType()))
                .toList();
        List<EquipmentProcedureFile> documents = equipmentProcedureFilesDc.getItems().stream()
                .filter(file -> FileType.DOCUMENT.equals(file.getFileType()))
                .toList();

        renderFileCards(photoCards, photos, PreviewMode.PHOTO, "photosEmpty.text");
        renderFileCards(videoCards, videos, PreviewMode.VIDEO, "videosEmpty.text");
        documentFilesDc.setItems(documents);
        updateDocumentButtons();
    }

    private void renderFileCards(Div cards, List<EquipmentProcedureFile> files, PreviewMode previewMode,
                                 String emptyMessageKey) {
        cards.removeAll();
        if (files.isEmpty()) {
            cards.add(createEmptyMessage(emptyMessageKey));
            return;
        }

        files.forEach(file -> cards.add(createFileCard(file, previewMode)));
    }

    private Div createFileCard(EquipmentProcedureFile file, PreviewMode previewMode) {
        Div card = new Div();
        card.addClassName("reference-file-card");
        card.getElement().setAttribute("role", "button");
        card.getElement().setAttribute("tabindex", "0");
        card.getElement().setAttribute("title", formatFileTitle(file));
        card.addClickListener(event -> openFilePreview(file, previewMode));

        if (PreviewMode.PHOTO.equals(previewMode) && file.getFileRef() != null) {
            Image thumbnail = new Image(FileRefResources.inlineResource(file.getFileRef(), fileStorageLocator),
                    formatFileTitle(file));
            thumbnail.addClassName("reference-file-card-media");
            card.add(thumbnail);
        } else {
            Icon icon = (PreviewMode.VIDEO.equals(previewMode) ? VaadinIcon.PLAY_CIRCLE : VaadinIcon.FILE).create();
            icon.addClassName("reference-file-card-icon");
            card.add(icon);
        }

        H5 title = new H5(formatFileTitle(file));
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

    private Span createEmptyMessage(String messageKey) {
        Span emptyMessage = new Span(messageBundle.getMessage(messageKey));
        emptyMessage.addClassName("reference-file-empty");
        return emptyMessage;
    }

    private void openDocumentPreview(EquipmentProcedureFile file) {
        openFilePreview(file, PreviewMode.DOCUMENT);
    }

    private void openFilePreview(EquipmentProcedureFile file, PreviewMode previewMode) {
        if (file == null || file.getFileRef() == null) {
            return;
        }

        dialogWindows.view(this, EquipmentProcedureFilePreviewView.class)
                .withViewConfigurer(previewView -> previewView.setPreviewFile(file, previewMode))
                .open();
    }

    private void updateDocumentButtons() {
        EquipmentProcedureFile selectedDocument = documentFilesDataGrid.getSingleSelectedItem();
        boolean hasFile = selectedDocument != null && selectedDocument.getFileRef() != null;

        viewDocumentButton.setEnabled(hasFile);
        downloadDocumentButton.setEnabled(hasFile);

        if (hasFile) {
            downloadDocumentAnchor.setHref(FileRefResources.downloadResource(selectedDocument.getFileRef(), fileStorageLocator));
        } else {
            downloadDocumentAnchor.removeHref();
        }
    }

    private String formatEstimatedTime(Integer estimatedMinutes) {
        if (estimatedMinutes == null) {
            return "";
        }

        int hours = estimatedMinutes / 60;
        int minutes = estimatedMinutes % 60;

        if (hours == 0) {
            return "%d %s".formatted(minutes, messageBundle.getMessage("estimatedTime.minutes"));
        }
        if (minutes == 0) {
            return "%d %s".formatted(hours, messageBundle.getMessage("estimatedTime.hours"));
        }

        return "%d %s %d %s".formatted(
                hours, messageBundle.getMessage("estimatedTime.hours"),
                minutes, messageBundle.getMessage("estimatedTime.minutes"));
    }

    private String formatChecklistStatement(EquipmentProcedureCheck procedureCheck) {
        return procedureCheck.getStatement() == null ? "" : procedureCheck.getStatement();
    }

    private String formatFileTitle(EquipmentProcedureFile file) {
        if (file.getTitle() != null && !file.getTitle().isBlank()) {
            return file.getTitle();
        }

        return formatFileName(file.getFileRef());
    }

    private String formatFileName(FileRef fileRef) {
        return fileRef == null || fileRef.getFileName() == null ? "" : fileRef.getFileName();
    }

    private String formatEquipmentModelName(EquipmentModel equipmentModel) {
        if (equipmentModel == null || equipmentModel.getName() == null || equipmentModel.getName().isBlank()) {
            return messageBundle.getMessage("procedureInformationSection.text");
        }

        return equipmentModel.getName();
    }

    private String formatInstructions(String instructions) {
        return instructions == null ? "" : instructions;
    }
}
