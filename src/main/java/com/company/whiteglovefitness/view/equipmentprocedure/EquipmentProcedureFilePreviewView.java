package com.company.whiteglovefitness.view.equipmentprocedure;

import com.company.whiteglovefitness.component.FileRefResources;
import com.company.whiteglovefitness.component.VideoPlayer;
import com.company.whiteglovefitness.entity.EquipmentProcedureFile;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "equipment-procedure-file-preview", layout = MainView.class)
@ViewController(id = "EquipmentProcedureFile.preview")
@ViewDescriptor(path = "equipment-procedure-file-preview-view.xml")
@DialogMode(width = "100%", height = "100%", closeOnEsc = true, closeOnOutsideClick = true, resizable = true)
public class EquipmentProcedureFilePreviewView extends StandardView {

    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 3.0;
    private static final double ZOOM_STEP = 0.25;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private Div previewToolbar;

    @ViewComponent
    private Div previewContent;

    private EquipmentProcedureFile previewFile;
    private PreviewMode previewMode;
    private Image previewImage;
    private double imageZoom = 1.0;

    public void setPreviewFile(EquipmentProcedureFile previewFile, PreviewMode previewMode) {
        this.previewFile = previewFile;
        this.previewMode = previewMode;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        setPageTitle(formatTitle(previewFile));
        renderPreview();
    }

    private void renderPreview() {
        previewToolbar.removeAll();
        previewContent.removeAll();

        if (previewFile == null || previewFile.getFileRef() == null) {
            previewContent.add(createEmptyMessage());
            return;
        }

        if (PreviewMode.PHOTO.equals(previewMode)) {
            renderPhotoPreview(previewFile.getFileRef());
        } else if (PreviewMode.VIDEO.equals(previewMode)) {
            renderVideoPreview(previewFile.getFileRef());
        } else {
            renderDocumentPreview(previewFile.getFileRef());
        }
    }

    private void renderPhotoPreview(FileRef fileRef) {
        previewToolbar.add(
                createToolbarButton("zoomInButton.text", VaadinIcon.SEARCH_PLUS, this::onZoomIn),
                createToolbarButton("zoomOutButton.text", VaadinIcon.SEARCH_MINUS, this::onZoomOut),
                createToolbarButton("resetZoomButton.text", VaadinIcon.REFRESH, this::onResetZoom)
        );

        previewImage = new Image(FileRefResources.inlineResource(fileRef, fileStorageLocator), formatTitle(previewFile));
        previewImage.addClassName("reference-preview-image");
        applyImageZoom();

        Div imageWrapper = new Div(previewImage);
        imageWrapper.addClassName("reference-preview-image-wrapper");
        previewContent.add(imageWrapper);
    }

    private void renderVideoPreview(FileRef fileRef) {
        VideoPlayer videoPlayer = new VideoPlayer();
        videoPlayer.addClassName("reference-preview-video");
        videoPlayer.setFileRef(fileRef, fileStorageLocator);
        previewContent.add(videoPlayer);
    }

    private void renderDocumentPreview(FileRef fileRef) {
        IFrame pdfViewer = new IFrame();
        pdfViewer.addClassName("reference-preview-document");
        pdfViewer.setSrc(FileRefResources.inlineResource(fileRef, fileStorageLocator));
        previewContent.add(pdfViewer);
    }

    private Button createToolbarButton(String messageKey, VaadinIcon icon,
                                       com.vaadin.flow.component.ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(messageBundle.getMessage(messageKey), icon.create(), listener);
        button.addClassName("reference-preview-toolbar-button");
        button.getElement().setAttribute("title", messageBundle.getMessage(messageKey));
        return button;
    }

    private void onZoomIn(ClickEvent<Button> event) {
        imageZoom = Math.min(MAX_ZOOM, imageZoom + ZOOM_STEP);
        applyImageZoom();
    }

    private void onZoomOut(ClickEvent<Button> event) {
        imageZoom = Math.max(MIN_ZOOM, imageZoom - ZOOM_STEP);
        applyImageZoom();
    }

    private void onResetZoom(ClickEvent<Button> event) {
        imageZoom = 1.0;
        applyImageZoom();
    }

    private void applyImageZoom() {
        if (previewImage != null) {
            previewImage.getStyle().set("transform", "scale(" + imageZoom + ")");
        }
    }

    private Span createEmptyMessage() {
        Span emptyMessage = new Span(messageBundle.getMessage("filePreview.noFile"));
        emptyMessage.addClassName("reference-file-empty");
        return emptyMessage;
    }

    private String formatTitle(EquipmentProcedureFile file) {
        if (file == null) {
            return messageBundle.getMessage("filePreviewView.title");
        }
        if (file.getTitle() != null && !file.getTitle().isBlank()) {
            return file.getTitle();
        }
        FileRef fileRef = file.getFileRef();
        return fileRef == null || fileRef.getFileName() == null || fileRef.getFileName().isBlank()
                ? messageBundle.getMessage("filePreviewView.title")
                : fileRef.getFileName();
    }

    public enum PreviewMode {
        PHOTO,
        VIDEO,
        DOCUMENT
    }
}
