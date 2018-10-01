package com.elevenst.dpm.view;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@SpringUI
@Title("Demand Forecaster for Dynamic Price v0.1")
@Theme("dynamicprice")
public class MainView extends UI {
    @Autowired
    private ApplicationContext ctx;

    private OneProductGrid topGrid;
    private OneProductGrid middleGrid;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        topGrid = (OneProductGrid) ctx.getBean("oneProductGrid");
        middleGrid = (OneProductGrid) ctx.getBean("oneProductGrid");
        topGrid.setProductNo("32939710");
        middleGrid.setProductNo("94291669");
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponents(topGrid.getOneProductGrid(), middleGrid.getOneProductGrid());
        setContent(mainLayout);
    }
}