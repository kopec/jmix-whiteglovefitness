package com.company.whiteglovefitness.view.equipmentfile;

import com.company.whiteglovefitness.component.VideoPlayer;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.upload.FileStorageUploadField;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;
import java.util.Set;


@Route(value = "equipment-files/:id", layout = MainView.class)
@ViewController(id = "EquipmentFile.detail")
@ViewDescriptor(path = "equipment-file-detail-view.xml")
@EditedEntityContainer("equipmentFileDc")
public class EquipmentFileDetailView extends StandardDetailView<EquipmentFile> {

    private static final Set<String> PHOTO_EXTENSIONS = Set.of(
            "bmp", "gif", "heic", "heif", "jpeg", "jpg", "png", "svg", "tif", "tiff", "webp");

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "3gp", "avi", "m4v", "mkv", "mov", "mp4", "mpeg", "mpg", "webm", "wmv");

    @Autowired
    private FileStorageLocator fileStorageLocator;

    @ViewComponent
    private JmixSelect<FileType> fileTypeField;

    @ViewComponent
    private FileStorageUploadField fileRefField;

    @ViewComponent
    private Component image;

    @ViewComponent
    private VerticalLayout previewBox;

    @ViewComponent
    private VideoPlayer videoPlayer;

    @Subscribe
    public void onInit(final InitEvent event) {
        fileRefField.setDropAllowed(false);

        videoPlayer.setMaxHeight("40em");
        videoPlayer.setWidth("100%");

        fileTypeField.addValueChangeListener(valueChangeEvent -> updatePreview());
        fileRefField.addValueChangeListener(valueChangeEvent -> {
            selectFileType(valueChangeEvent.getValue(), true);
            updatePreview();
        });
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        selectFileType(getEditedEntity().getFileRef(), false);
        updatePreview();
    }

    private void selectFileType(FileRef fileRef, boolean replaceExistingType) {
        FileType detectedType = detectFileType(fileRef);
        if (replaceExistingType || fileTypeField.getValue() == null) {
            fileTypeField.setValue(detectedType);
        }
    }

    private FileType detectFileType(FileRef fileRef) {
        if (fileRef == null) {
            return null;
        }

        String contentType = fileRef.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return FileType.PHOTO;
            }
            if (contentType.startsWith("video/")) {
                return FileType.VIDEO;
            }
        }

        String fileName = fileRef.getFileName();
        String extension = fileName == null ? "" : fileName.substring(fileName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);

        if (PHOTO_EXTENSIONS.contains(extension)) {
            return FileType.PHOTO;
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return FileType.VIDEO;
        }
        return FileType.DOCUMENT;
    }

    private void updatePreview() {
        EquipmentFile file = getEditedEntity();
        boolean video = FileType.VIDEO.equals(file.getFileType()) && file.getFileRef() != null;
        boolean photo = FileType.PHOTO.equals(file.getFileType()) && file.getFileRef() != null;

        previewBox.setVisible(photo || video);
        image.setVisible(photo);
        videoPlayer.setVisible(video);
        if (video) {
            videoPlayer.setFileRef(file.getFileRef(), fileStorageLocator);
        } else {
            videoPlayer.clearSource();
        }
    }
}
