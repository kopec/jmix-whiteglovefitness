package com.company.whiteglovefitness.equipmentfile;

import com.company.whiteglovefitness.WhiteGloveFitnessApplication;
import com.company.whiteglovefitness.component.VideoPlayer;
import com.company.whiteglovefitness.entity.EquipmentFile;
import com.company.whiteglovefitness.entity.FileType;
import com.company.whiteglovefitness.view.equipmentfile.EquipmentFileDetailView;
import com.company.whiteglovefitness.view.equipmentfile.EquipmentFilePreviewView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.DataManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorageLocator;
import io.jmix.flowui.ViewNavigators;
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
import java.nio.file.Files;
import java.nio.file.Path;

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

        Component image = UiTestUtils.getComponent(detailView, "image");
        VerticalLayout previewBox = UiTestUtils.getComponent(detailView, "previewBox");
        Component videoPlayer = UiTestUtils.getComponent(detailView, "videoPlayer");

        Assertions.assertEquals("com.company.whiteglovefitness.component.VideoPlayer",
                videoPlayer.getClass().getName());
        Assertions.assertFalse(image.isVisible());
        Assertions.assertFalse(previewBox.isVisible());
        Assertions.assertFalse(videoPlayer.isVisible());
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

        EquipmentFilePreviewView previewView = UiTestUtils.getCurrentView();
        setField(previewView, "fileStorageLocator", fileStorageLocator);
        previewView.setPreviewFile(videoFile);
        previewView.onBeforeShow(null);

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
}
