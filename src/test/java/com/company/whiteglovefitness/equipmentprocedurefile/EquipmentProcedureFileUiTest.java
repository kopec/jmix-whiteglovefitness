package com.company.whiteglovefitness.equipmentprocedurefile;

import com.company.whiteglovefitness.WhiteGloveFitnessApplication;
import com.company.whiteglovefitness.entity.EquipmentProcedureFile;
import com.company.whiteglovefitness.view.equipmentprocedurefile.EquipmentProcedureFileDetailView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@UiTest
@SpringBootTest(classes = {WhiteGloveFitnessApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
public class EquipmentProcedureFileUiTest {

    @Autowired
    ViewNavigators viewNavigators;

    @Test
    void opensDetailViewWithVideoPreview() {
        viewNavigators.detailView(UiTestUtils.getCurrentView(), EquipmentProcedureFile.class)
                .newEntity()
                .withViewClass(EquipmentProcedureFileDetailView.class)
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
}
