package com.company.whiteglovefitness.view.equipmentmodel;

import com.company.whiteglovefitness.component.FileRefResources;
import com.company.whiteglovefitness.entity.EquipmentMeasurement;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.EquipmentFileCardRenderer;
import com.company.whiteglovefitness.view.EquipmentViewFormatter;
import com.company.whiteglovefitness.view.equipmentfile.EquipmentFilePreviewView;
import com.company.whiteglovefitness.view.equipmentprocedure.EquipmentProcedureReferenceView;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "equipment-model-references/:id", layout = MainView.class)
@ViewController(id = "EquipmentModel.reference")
@ViewDescriptor(path = "equipment-model-reference-view.xml")
@EditedEntityContainer("equipmentModelDc")
public class EquipmentModelReferenceView extends StandardDetailView<EquipmentModel> {

    private static final String PROCEDURE_REFERENCE_VERTICAL_LAYOUT_PADDING =
            "var(--vaadin-vertical-layout-padding, var(--vaadin-padding-m))";
    private static final String PROCEDURE_REFERENCE_SCROLLBAR_WIDTH =
            "var(--reference-dialog-scrollbar-width, 17px)";

    @Autowired
    private DialogWindows dialogWindows;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    @Autowired
    private EquipmentViewFormatter equipmentViewFormatter;

    @Autowired
    private EquipmentFileCardRenderer equipmentFileCardRenderer;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private CollectionContainer<EquipmentFile> equipmentFilesDc;

    @ViewComponent
    private CollectionContainer<EquipmentFile> documentFilesDc;

    @ViewComponent
    private Div photoCards;

    @ViewComponent
    private Div videoCards;

    @ViewComponent
    private Markdown descriptionField;

    @ViewComponent
    private HorizontalLayout documentFilesButtonsPanel;

    @ViewComponent
    private DataGrid<EquipmentFile> documentFilesDataGrid;

    @ViewComponent
    private DataGrid<EquipmentProcedure> equipmentProceduresDataGrid;

    @ViewComponent
    private JmixButton openProcedureButton;

    private Button viewDocumentButton;
    private Button downloadDocumentButton;
    private Anchor downloadDocumentAnchor;

    @Subscribe
    public void onInit(final InitEvent event) {
        initDocumentButtons();
        documentFilesDataGrid.addSelectionListener(selectionEvent -> updateDocumentButtons());
        documentFilesDataGrid.addItemDoubleClickListener(doubleClickEvent -> openFilePreview(doubleClickEvent.getItem()));
        documentFilesDataGrid.setEmptyStateText(messageBundle.getMessage("documentsEmpty.text"));
        equipmentProceduresDataGrid.addItemDoubleClickListener(
                doubleClickEvent -> openProcedureReference(doubleClickEvent.getItem()));
        equipmentProceduresDataGrid.addSelectionListener(
                selectionEvent -> updateOpenProcedureButtonState());
        updateOpenProcedureButtonState();
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        descriptionField.setContent(formatDescription(getEditedEntity().getDescription()));
        populateFileSections();
    }

    @Subscribe("openProcedureButton")
    public void onOpenProcedureButtonClick(final ClickEvent<JmixButton> event) {
        openProcedureReference(equipmentProceduresDataGrid.getSingleSelectedItem());
    }

    @Supply(to = "documentFilesDataGrid.fileRef", subject = "renderer")
    private Renderer<EquipmentFile> documentFilesDataGridFileRenderer() {
        return new TextRenderer<>(equipmentFile -> equipmentViewFormatter.formatFileName(equipmentFile.getFileRef()));
    }

    @Supply(to = "equipmentMeasurementsDataGrid.valueAndUnit", subject = "renderer")
    private Renderer<EquipmentMeasurement> equipmentMeasurementsDataGridValueAndUnitRenderer() {
        return new TextRenderer<>(equipmentViewFormatter::formatMeasurementValueAndUnit);
    }

    @Supply(to = "equipmentProceduresDataGrid.fileCounts", subject = "renderer")
    private Renderer<EquipmentProcedure> equipmentProceduresDataGridFileCountsRenderer() {
        return new TextRenderer<>(equipmentViewFormatter::formatProcedureFileCounts);
    }

    @Supply(to = "equipmentProceduresDataGrid.estimatedMinutes", subject = "renderer")
    private Renderer<EquipmentProcedure> equipmentProceduresDataGridEstimatedMinutesRenderer() {
        return new TextRenderer<>(procedure -> equipmentViewFormatter.formatEstimatedTime(procedure.getEstimatedMinutes()));
    }

    private void updateOpenProcedureButtonState() {
        openProcedureButton.setEnabled(equipmentProceduresDataGrid.getSingleSelectedItem() != null);
    }

    private void initDocumentButtons() {
        viewDocumentButton = new Button(messageBundle.getMessage("viewDocumentButton.text"),
                VaadinIcon.EYE.create(),
                event -> openFilePreview(documentFilesDataGrid.getSingleSelectedItem()));
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
        List<EquipmentFile> photos = equipmentFileCardRenderer.filterFilesByType(equipmentFilesDc.getItems(), FileType.PHOTO);
        List<EquipmentFile> videos = equipmentFileCardRenderer.filterFilesByType(equipmentFilesDc.getItems(), FileType.VIDEO);
        List<EquipmentFile> documents = equipmentFileCardRenderer.filterFilesByType(equipmentFilesDc.getItems(), FileType.DOCUMENT);

        equipmentFileCardRenderer.renderFileCards(photoCards, photos, FileType.PHOTO,
                messageBundle.getMessage("photosEmpty.text"),
                this::openFilePreview);
        equipmentFileCardRenderer.renderFileCards(videoCards, videos, FileType.VIDEO,
                messageBundle.getMessage("videosEmpty.text"),
                this::openFilePreview);
        documentFilesDc.setItems(documents);
        updateDocumentButtons();
    }

    private void updateDocumentButtons() {
        EquipmentFile selectedDocument = documentFilesDataGrid.getSingleSelectedItem();
        boolean hasFile = selectedDocument != null && selectedDocument.getFileRef() != null;

        viewDocumentButton.setEnabled(hasFile);
        downloadDocumentButton.setEnabled(hasFile);

        if (hasFile) {
            downloadDocumentAnchor.setHref(FileRefResources.downloadResource(selectedDocument.getFileRef(), fileStorageLocator));
        } else {
            downloadDocumentAnchor.removeHref();
        }
    }

    private void openFilePreview(EquipmentFile file) {
        if (file == null || file.getFileRef() == null) {
            return;
        }

        dialogWindows.view(this, EquipmentFilePreviewView.class)
                .withViewConfigurer(previewView -> previewView.setPreviewFile(file))
                .open();
    }

    private void openProcedureReference(EquipmentProcedure procedure) {
        if (procedure == null) {
            return;
        }

        DialogWindow<EquipmentProcedureReferenceView> dialogWindow = dialogWindows.detail(this, EquipmentProcedure.class)
                .editEntity(procedure)
                .withViewClass(EquipmentProcedureReferenceView.class)
                .build();
        applyProcedureReferenceDialogWidth(dialogWindow, procedure);
        dialogWindow.open();
    }

    private void applyProcedureReferenceDialogWidth(DialogWindow<EquipmentProcedureReferenceView> dialogWindow,
                                                    EquipmentProcedure procedure) {
        int cardCount = Math.max(countFiles(procedure, FileType.PHOTO), countFiles(procedure, FileType.VIDEO));
        if (cardCount > 0) {
            dialogWindow.setMinWidth(buildProcedureReferenceDialogMinWidth(cardCount));
        }
        dialogWindow.setMaxWidth("100%");
    }

    private String buildProcedureReferenceDialogMinWidth(int cardCount) {
        int gaps = cardCount - 1;
        return "min(calc(%d * 12rem + %d * var(--vaadin-gap-m) + 2 * %s + %s), 100%%)"
                .formatted(cardCount, gaps,
                        PROCEDURE_REFERENCE_VERTICAL_LAYOUT_PADDING,
                        PROCEDURE_REFERENCE_SCROLLBAR_WIDTH);
    }

    private int countFiles(EquipmentProcedure procedure, FileType fileType) {
        List<EquipmentFile> files = procedure.getEquipmentFiles();
        if (files == null) {
            return 0;
        }

        return (int) files.stream()
                .map(EquipmentFile::getFileType)
                .filter(fileType::equals)
                .count();
    }

    private String formatDescription(String description) {
        return description == null ? "" : description;
    }
}
