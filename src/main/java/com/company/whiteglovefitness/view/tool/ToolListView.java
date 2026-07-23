package com.company.whiteglovefitness.view.tool;

import com.company.whiteglovefitness.entity.Tool;

import com.company.whiteglovefitness.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "tools", layout = MainView.class)
@ViewController(id = "Tool.list")
@ViewDescriptor(path = "tool-list-view.xml")
@LookupComponent("toolsDataGrid")
@DialogMode(width = "64em")
public class ToolListView extends StandardListView<Tool> {

}