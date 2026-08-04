package com.company.whiteglovefitness.view.equipmentfile;

import com.company.whiteglovefitness.component.VideoPlayer;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
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


@Route(value = "equipment-files/:id", layout = MainView.class)
@ViewController(id = "EquipmentFile.detail")
@ViewDescriptor(path = "equipment-file-detail-view.xml")
@EditedEntityContainer("equipmentFileDc")
public class EquipmentFileDetailView extends StandardDetailView<EquipmentFile> {

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
        fileRefField.addValueChangeListener(valueChangeEvent -> updatePreview());
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        updatePreview();
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
