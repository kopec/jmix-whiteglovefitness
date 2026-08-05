package com.company.whiteglovefitness.view.equipmentfile;

import com.company.whiteglovefitness.component.FileRefResources;
import com.company.whiteglovefitness.component.VideoPlayer;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.DetachEvent;
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
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "equipment-file-preview", layout = MainView.class)
@ViewController(id = "EquipmentFile.preview")
@ViewDescriptor(path = "equipment-file-preview-view.xml")
@DialogMode(width = "100%", height = "100%", closeOnEsc = true, closeOnOutsideClick = true, resizable = true)
public class EquipmentFilePreviewView extends StandardView {

    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 4.0;
    private static final double ZOOM_STEP = 0.25;
    private static final String IMAGE_ZOOM_CHANGED_EVENT = "image-zoom-changed";

    @Autowired
    private FileStorageLocator fileStorageLocator;

    @ViewComponent
    private MessageBundle messageBundle;

    @ViewComponent
    private Div previewToolbar;

    @ViewComponent
    private Div previewContent;

    private EquipmentFile previewFile;
    private Div previewImageWrapper;
    private Div previewImageStage;
    private Image previewImage;
    private double imageZoom = 1.0;

    public void setPreviewFile(EquipmentFile previewFile) {
        this.previewFile = previewFile;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        setPageTitle(formatTitle(previewFile));
        renderPreview();
    }

    private void renderPreview() {
        cleanupImageZoom();
        previewToolbar.removeAll();
        previewContent.removeAll();
        previewImageWrapper = null;
        previewImageStage = null;
        previewImage = null;

        if (previewFile == null || previewFile.getFileRef() == null) {
            previewContent.add(createEmptyMessage());
            return;
        }

        FileType fileType = previewFile.getFileType();
        if (FileType.PHOTO.equals(fileType)) {
            renderPhotoPreview(previewFile.getFileRef());
        } else if (FileType.VIDEO.equals(fileType)) {
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

        previewImageStage = new Div(previewImage);
        previewImageStage.addClassName("reference-preview-image-stage");

        Div imageWrapper = new Div(previewImageStage);
        imageWrapper.addClassName("reference-preview-image-wrapper");
        previewImageWrapper = imageWrapper;
        previewContent.add(imageWrapper);
        enableTouchZoom(imageWrapper);
        applyImageZoom();
    }

    private void enableTouchZoom(Div imageWrapper) {
        imageWrapper.getElement()
                .addEventListener(IMAGE_ZOOM_CHANGED_EVENT, event -> {
                    double zoom = event.getEventData().get("event.detail.zoom").asDouble(imageZoom);
                    imageZoom = clampZoom(zoom);
                    applyImageZoom();
                })
                .addEventData("event.detail.zoom");

        imageWrapper.getElement().executeJs("""
                const wrapper = this;
                const stage = $0;
                const image = $1;
                const minZoom = $2;
                const maxZoom = $3;

                if (wrapper.__wgfImageZoomCleanup) {
                    wrapper.__wgfImageZoomCleanup();
                }

                let startDistance = 0;
                let startZoom = Number(image.getAttribute('data-zoom')) || 1;
                let lastMidpoint = null;
                let baseWidth = 0;
                let baseHeight = 0;

                const distance = touches => {
                    const first = touches[0];
                    const second = touches[1];
                    return Math.hypot(second.clientX - first.clientX, second.clientY - first.clientY);
                };
                const midpoint = touches => {
                    const first = touches[0];
                    const second = touches[1];
                    return {
                        clientX: (first.clientX + second.clientX) / 2,
                        clientY: (first.clientY + second.clientY) / 2
                    };
                };
                const clamp = value => Math.min(maxZoom, Math.max(minZoom, value));
                const clampRatio = value => Math.min(1, Math.max(0, value));
                const clampScroll = (value, max) => Math.min(Math.max(0, value), Math.max(0, max));
                const visibleCenter = () => {
                    const wrapperRect = wrapper.getBoundingClientRect();
                    return {
                        clientX: wrapperRect.left + wrapperRect.width / 2,
                        clientY: wrapperRect.top + wrapperRect.height / 2
                    };
                };
                const updateBaseSize = () => {
                    if (!image.naturalWidth || !image.naturalHeight || !wrapper.clientWidth || !wrapper.clientHeight) {
                        baseWidth = image.offsetWidth || wrapper.clientWidth;
                        baseHeight = image.offsetHeight || wrapper.clientHeight;
                        return;
                    }

                    const fit = Math.min(
                        wrapper.clientWidth / image.naturalWidth,
                        wrapper.clientHeight / image.naturalHeight,
                        1
                    );
                    baseWidth = Math.max(1, Math.floor(image.naturalWidth * fit));
                    baseHeight = Math.max(1, Math.floor(image.naturalHeight * fit));
                    image.style.width = `${baseWidth}px`;
                    image.style.height = `${baseHeight}px`;
                };
                const updateStageSize = zoom => {
                    updateBaseSize();
                    if (zoom <= 1) {
                        stage.style.width = '100%';
                        stage.style.height = '100%';
                        return;
                    }

                    stage.style.width = `${Math.max(wrapper.clientWidth, Math.ceil(baseWidth * zoom))}px`;
                    stage.style.height = `${Math.max(wrapper.clientHeight, Math.ceil(baseHeight * zoom))}px`;
                };
                const setZoom = (value, focalPoint = null) => {
                    const beforeRect = focalPoint ? image.getBoundingClientRect() : null;
                    const focalRatioX = beforeRect && beforeRect.width > 0
                        ? clampRatio((focalPoint.clientX - beforeRect.left) / beforeRect.width)
                        : 0.5;
                    const focalRatioY = beforeRect && beforeRect.height > 0
                        ? clampRatio((focalPoint.clientY - beforeRect.top) / beforeRect.height)
                        : 0.5;
                    const zoom = clamp(value);
                    image.setAttribute('data-zoom', String(zoom));
                    image.style.transform = `scale(${zoom})`;
                    updateStageSize(zoom);

                    if (focalPoint && beforeRect && beforeRect.width > 0 && beforeRect.height > 0) {
                        const afterRect = image.getBoundingClientRect();
                        const focusedClientX = afterRect.left + afterRect.width * focalRatioX;
                        const focusedClientY = afterRect.top + afterRect.height * focalRatioY;
                        wrapper.scrollLeft = clampScroll(
                            wrapper.scrollLeft + focusedClientX - focalPoint.clientX,
                            wrapper.scrollWidth - wrapper.clientWidth
                        );
                        wrapper.scrollTop = clampScroll(
                            wrapper.scrollTop + focusedClientY - focalPoint.clientY,
                            wrapper.scrollHeight - wrapper.clientHeight
                        );
                    }

                    return zoom;
                };
                const refreshZoom = () => setZoom(Number(image.getAttribute('data-zoom')) || 1);
                const setZoomAtVisibleCenter = value => setZoom(value, visibleCenter());
                const onTouchStart = event => {
                    if (event.touches.length === 2) {
                        event.preventDefault();
                        startDistance = distance(event.touches);
                        startZoom = Number(image.getAttribute('data-zoom')) || 1;
                        lastMidpoint = midpoint(event.touches);
                    }
                };
                const onTouchMove = event => {
                    if (event.touches.length === 2 && startDistance > 0) {
                        event.preventDefault();
                        const currentMidpoint = midpoint(event.touches);
                        if (lastMidpoint) {
                            wrapper.scrollLeft = clampScroll(
                                wrapper.scrollLeft + lastMidpoint.clientX - currentMidpoint.clientX,
                                wrapper.scrollWidth - wrapper.clientWidth
                            );
                            wrapper.scrollTop = clampScroll(
                                wrapper.scrollTop + lastMidpoint.clientY - currentMidpoint.clientY,
                                wrapper.scrollHeight - wrapper.clientHeight
                            );
                        }
                        setZoom(startZoom * distance(event.touches) / startDistance, currentMidpoint);
                        lastMidpoint = currentMidpoint;
                    }
                };
                const onTouchEnd = event => {
                    if (event.touches.length < 2 && startDistance > 0) {
                        startDistance = 0;
                        lastMidpoint = null;
                        wrapper.dispatchEvent(new CustomEvent('image-zoom-changed', {
                            detail: {zoom: Number(image.getAttribute('data-zoom')) || 1}
                        }));
                    }
                };

                wrapper.addEventListener('touchstart', onTouchStart, {passive: false});
                wrapper.addEventListener('touchmove', onTouchMove, {passive: false});
                wrapper.addEventListener('touchend', onTouchEnd);
                wrapper.addEventListener('touchcancel', onTouchEnd);
                window.addEventListener('resize', refreshZoom);
                image.addEventListener('load', refreshZoom);
                stage.__wgfRefreshZoomStage = refreshZoom;
                stage.__wgfSetZoomAtVisibleCenter = setZoomAtVisibleCenter;
                refreshZoom();
                wrapper.__wgfImageZoomCleanup = () => {
                    wrapper.removeEventListener('touchstart', onTouchStart);
                    wrapper.removeEventListener('touchmove', onTouchMove);
                    wrapper.removeEventListener('touchend', onTouchEnd);
                    wrapper.removeEventListener('touchcancel', onTouchEnd);
                    window.removeEventListener('resize', refreshZoom);
                    image.removeEventListener('load', refreshZoom);
                    delete stage.__wgfRefreshZoomStage;
                    delete stage.__wgfSetZoomAtVisibleCenter;
                    delete wrapper.__wgfImageZoomCleanup;
                };
                """, previewImageStage.getElement(), previewImage.getElement(), MIN_ZOOM, MAX_ZOOM);
    }

    private void renderVideoPreview(FileRef fileRef) {
        VideoPlayer videoPlayer = new VideoPlayer();
        videoPlayer.addClassName("reference-preview-video");
        videoPlayer.setAutoplay(true);
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
        String label = messageBundle.getMessage(messageKey);
        Button button = new Button(icon.create(), listener);
        button.addClassName("reference-preview-toolbar-button");
        button.setAriaLabel(label);
        button.getElement().setAttribute("title", label);
        return button;
    }

    private void onZoomIn(ClickEvent<Button> event) {
        imageZoom = clampZoom(imageZoom + ZOOM_STEP);
        applyImageZoomAtVisibleCenter();
    }

    private void onZoomOut(ClickEvent<Button> event) {
        imageZoom = clampZoom(imageZoom - ZOOM_STEP);
        applyImageZoomAtVisibleCenter();
    }

    private void onResetZoom(ClickEvent<Button> event) {
        imageZoom = 1.0;
        applyImageZoomAtVisibleCenter();
    }

    private void applyImageZoom() {
        if (previewImage != null) {
            previewImage.getStyle().set("transform", "scale(" + imageZoom + ")");
            previewImage.getElement().setAttribute("data-zoom", Double.toString(imageZoom));
        }
        if (previewImageStage != null) {
            previewImageStage.getElement()
                    .executeJs("this.__wgfRefreshZoomStage && this.__wgfRefreshZoomStage();");
        }
    }

    private void applyImageZoomAtVisibleCenter() {
        if (previewImageStage != null && previewImage != null) {
            previewImageStage.getElement().executeJs("""
                    if (this.__wgfSetZoomAtVisibleCenter) {
                        this.__wgfSetZoomAtVisibleCenter($0);
                    } else if (this.__wgfRefreshZoomStage) {
                        const image = $1;
                        image.setAttribute('data-zoom', String($0));
                        image.style.transform = 'scale(' + $0 + ')';
                        this.__wgfRefreshZoomStage();
                    }
                    """, imageZoom, previewImage.getElement());
        }
    }

    private void cleanupImageZoom() {
        if (previewImageWrapper != null) {
            previewImageWrapper.getElement()
                    .executeJs("this.__wgfImageZoomCleanup && this.__wgfImageZoomCleanup();");
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        cleanupImageZoom();
        super.onDetach(detachEvent);
    }

    private double clampZoom(double zoom) {
        return Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, zoom));
    }

    private Span createEmptyMessage() {
        Span emptyMessage = new Span(messageBundle.getMessage("filePreview.noFile"));
        emptyMessage.addClassName("reference-file-empty");
        return emptyMessage;
    }

    private String formatTitle(EquipmentFile file) {
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

}
