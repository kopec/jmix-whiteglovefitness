package com.company.whiteglovefitness.equipmentfile;

import com.company.whiteglovefitness.WhiteGloveFitnessApplication;
import com.company.whiteglovefitness.component.VideoPlayer;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.equipmentfile.EquipmentFileDetailView;
import com.company.whiteglovefitness.view.equipmentfile.EquipmentFilePreviewView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.upload.FileStorageUploadField;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.localfs.LocalFileStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@UiTest
@SpringBootTest(classes = {WhiteGloveFitnessApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
public class EquipmentFileUiTest {

    @Autowired
    ViewNavigators viewNavigators;

    @Autowired
    DataManager dataManager;

    @TempDir
    private Path tempDir;

    @Test
    void opensDetailViewWithVideoPreview() {
        viewNavigators.detailView(UiTestUtils.getCurrentView(), EquipmentFile.class)
                .newEntity()
                .withViewClass(EquipmentFileDetailView.class)
                .navigate();

        View<?> detailView = UiTestUtils.getCurrentView();

        JmixSelect<FileType> fileTypeField = UiTestUtils.getComponent(detailView, "fileTypeField");
        Component image = UiTestUtils.getComponent(detailView, "image");
        VerticalLayout previewBox = UiTestUtils.getComponent(detailView, "previewBox");
        Component videoPlayer = UiTestUtils.getComponent(detailView, "videoPlayer");

        Assertions.assertTrue(fileTypeField.isReadOnly());
        Assertions.assertEquals("com.company.whiteglovefitness.component.VideoPlayer",
                videoPlayer.getClass().getName());
        Assertions.assertFalse(image.isVisible());
        Assertions.assertFalse(previewBox.isVisible());
        Assertions.assertFalse(videoPlayer.isVisible());
    }

    @Test
    void detectsFileTypeFromSelectedFile() throws Exception {
        Path videoPath = tempDir.resolve("2026/08/04/setup-video.mp4");
        Files.createDirectories(videoPath.getParent());
        Files.write(videoPath, new byte[]{0, 1, 2, 3});

        viewNavigators.detailView(UiTestUtils.getCurrentView(), EquipmentFile.class)
                .newEntity()
                .withViewClass(EquipmentFileDetailView.class)
                .navigate();

        EquipmentFileDetailView detailView = UiTestUtils.getCurrentView();
        FileStorageLocator fileStorageLocator = Mockito.mock(FileStorageLocator.class);
        Mockito.when(fileStorageLocator.getByName("fs"))
                .thenReturn(new LocalFileStorage("fs", tempDir.toString()));
        setField(detailView, "fileStorageLocator", fileStorageLocator);

        JmixSelect<FileType> fileTypeField = UiTestUtils.getComponent(detailView, "fileTypeField");
        FileStorageUploadField fileRefField = UiTestUtils.getComponent(detailView, "fileRefField");

        Assertions.assertTrue(fileTypeField.isReadOnly());
        Assertions.assertNull(fileTypeField.getValue());

        fileRefField.setValue(FileRef.create("fs", "test/model-photo.JPG", "model-photo.JPG"));

        Assertions.assertEquals(FileType.PHOTO, fileTypeField.getValue());

        fileRefField.setValue(FileRef.create("fs", "2026/08/04/setup-video.mp4", "setup-video.mp4"));

        Assertions.assertEquals(FileType.VIDEO, fileTypeField.getValue());

        fileRefField.setValue(FileRef.create("fs", "test/owner-manual.pdf", "owner-manual.pdf"));

        Assertions.assertEquals(FileType.DOCUMENT, fileTypeField.getValue());

        fileRefField.setValue(null);

        Assertions.assertNull(fileTypeField.getValue());
    }

    @Test
    void photoPreviewUsesCompactZoomControls() throws Exception {
        EquipmentFile photoFile = dataManager.create(EquipmentFile.class);
        photoFile.setFileType(FileType.PHOTO);
        photoFile.setFileRef(FileRef.create("fs", "test/model-photo.jpg", "model-photo.jpg"));

        viewNavigators.view(UiTestUtils.getCurrentView(), EquipmentFilePreviewView.class)
                .navigate();

        View<?> previewView = UiTestUtils.getCurrentView();
        invokeMethod(previewView, "setPreviewFile", photoFile);
        invokeMethod(previewView, "onBeforeShow", (Object) null);

        Div previewToolbar = UiTestUtils.getComponent(previewView, "previewToolbar");
        List<Button> buttons = previewToolbar.getChildren()
                .map(Button.class::cast)
                .toList();

        Assertions.assertEquals(3, buttons.size());
        Assertions.assertTrue(buttons.stream().allMatch(button -> button.getText().isBlank()));
        Assertions.assertTrue(buttons.stream()
                .map(button -> button.getAriaLabel().orElse(""))
                .toList()
                .containsAll(List.of("Zoom in", "Zoom out", "Reset zoom")));

        Div previewContent = UiTestUtils.getComponent(previewView, "previewContent");
        Div imageWrapper = previewContent.getChildren()
                .filter(Div.class::isInstance)
                .map(Div.class::cast)
                .findFirst()
                .orElseThrow();
        Div imageStage = imageWrapper.getChildren()
                .filter(Div.class::isInstance)
                .map(Div.class::cast)
                .findFirst()
                .orElseThrow();
        Image previewImage = imageStage.getChildren()
                .filter(Image.class::isInstance)
                .map(Image.class::cast)
                .findFirst()
                .orElseThrow();

        Assertions.assertTrue(imageWrapper.hasClassName("reference-preview-image-wrapper"));
        Assertions.assertTrue(imageStage.hasClassName("reference-preview-image-stage"));
        Assertions.assertEquals("scale(1.0)", previewImage.getStyle().get("transform"));
    }

    @Test
    void videoPreviewAutoplays() throws Exception {
        Path filePath = tempDir.resolve("2026/08/04/preview-video.mp4");
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, new byte[]{0, 1, 2, 3});

        FileStorageLocator fileStorageLocator = Mockito.mock(FileStorageLocator.class);
        Mockito.when(fileStorageLocator.getByName("fs"))
                .thenReturn(new LocalFileStorage("fs", tempDir.toString()));

        EquipmentFile videoFile = dataManager.create(EquipmentFile.class);
        videoFile.setFileType(FileType.VIDEO);
        videoFile.setFileRef(FileRef.create("fs", "2026/08/04/preview-video.mp4", "preview-video.mp4"));

        viewNavigators.view(UiTestUtils.getCurrentView(), EquipmentFilePreviewView.class)
                .navigate();

        View<?> previewView = UiTestUtils.getCurrentView();
        setField(previewView, "fileStorageLocator", fileStorageLocator);
        invokeMethod(previewView, "setPreviewFile", videoFile);
        invokeMethod(previewView, "onBeforeShow", (Object) null);

        Div previewContent = UiTestUtils.getComponent(previewView, "previewContent");
        Component preview = previewContent.getChildren().findFirst().orElseThrow();

        Assertions.assertInstanceOf(VideoPlayer.class, preview);
        Assertions.assertTrue(preview.getElement().getProperty("autoplay", false));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void invokeMethod(Object target, String methodName, Object... arguments) throws Exception {
        Method method = List.of(target.getClass().getMethods()).stream()
                .filter(candidate -> methodName.equals(candidate.getName()))
                .filter(candidate -> candidate.getParameterCount() == arguments.length)
                .findFirst()
                .orElseThrow();
        method.invoke(target, arguments);
    }
}
