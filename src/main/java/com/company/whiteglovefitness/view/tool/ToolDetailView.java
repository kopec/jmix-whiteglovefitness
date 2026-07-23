package com.company.whiteglovefitness.view.tool;

import com.company.whiteglovefitness.entity.Tool;
import com.company.whiteglovefitness.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "tools/:id", layout = MainView.class)
@ViewController(id = "Tool.detail")
@ViewDescriptor(path = "tool-detail-view.xml")
@EditedEntityContainer("toolDc")
public class ToolDetailView extends StandardDetailView<Tool> {
}