package com.company.whiteglovefitness.view.equipmentprocedure;

import com.company.whiteglovefitness.component.FileRefResources;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.EquipmentModel;
import com.company.whiteglovefitness.entity.EquipmentProcedure;
import com.company.whiteglovefitness.entity.EquipmentProcedureCheck;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.EquipmentFileCardRenderer;
import com.company.whiteglovefitness.view.EquipmentViewFormatter;
import com.company.whiteglovefitness.view.equipmentfile.EquipmentFilePreviewView;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
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

    @Autowired
    private EquipmentViewFormatter equipmentViewFormatter;

    @Autowired
    private EquipmentFileCardRenderer equipmentFileCardRenderer;

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
    private CollectionContainer<EquipmentFile> equipmentFilesDc;

    @ViewComponent
    private CollectionContainer<EquipmentFile> documentFilesDc;

    @ViewComponent
    private Div photoCards;

    @ViewComponent
    private Div videoCards;

    @ViewComponent
    private HorizontalLayout documentFilesButtonsPanel;

    @ViewComponent
    private DataGrid<EquipmentFile> documentFilesDataGrid;

    private Button viewDocumentButton;
    private Button downloadDocumentButton;
    private Anchor downloadDocumentAnchor;

    private Set<EquipmentProcedureCheck> checkedProcedureChecks = new LinkedHashSet<>();

    @Subscribe
    public void onInit(final InitEvent event) {
        initDocumentButtons();
        documentFilesDataGrid.addSelectionListener(selectionEvent -> updateDocumentButtons());
        documentFilesDataGrid.addItemDoubleClickListener(doubleClickEvent -> openFilePreview(doubleClickEvent.getItem()));
        documentFilesDataGrid.setEmptyStateText(messageBundle.getMessage("documentsEmpty.text"));
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        equipmentModelNameHeading.setText(formatEquipmentModelName(getEditedEntity().getEquipmentModel()));
        estimatedTimeField.setValue(equipmentViewFormatter.formatEstimatedTime(getEditedEntity().getEstimatedMinutes()));
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
    private Renderer<EquipmentFile> documentFilesDataGridFileRenderer() {
        return new TextRenderer<>(equipmentFile -> equipmentViewFormatter.formatFileName(equipmentFile.getFileRef()));
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

    private void openFilePreview(EquipmentFile file) {
        if (file == null || file.getFileRef() == null) {
            return;
        }

        dialogWindows.view(this, EquipmentFilePreviewView.class)
                .withViewConfigurer(previewView -> previewView.setPreviewFile(file))
                .open();
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

    private String formatChecklistStatement(EquipmentProcedureCheck procedureCheck) {
        return procedureCheck.getStatement() == null ? "" : procedureCheck.getStatement();
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
