package com.elevenst.dpm.view;

import com.byteowls.vaadin.chartjs.ChartJs;
import com.byteowls.vaadin.chartjs.config.LineChartConfig;
import com.byteowls.vaadin.chartjs.data.Dataset;
import com.byteowls.vaadin.chartjs.data.LineDataset;
import com.byteowls.vaadin.chartjs.options.InteractionMode;
import com.byteowls.vaadin.chartjs.options.Position;
import com.byteowls.vaadin.chartjs.options.scale.Axis;
import com.byteowls.vaadin.chartjs.options.scale.CategoryScale;
import com.byteowls.vaadin.chartjs.options.scale.LinearScale;
import com.byteowls.vaadin.chartjs.utils.ColorUtils;
import com.elevenst.dpm.fcmodel.ForecastModel;
import com.elevenst.dpm.fcmodel.ForecastResult;
import com.elevenst.dpm.repository.DailyOrder;
import com.elevenst.dpm.repository.SMAPEResult;
import com.elevenst.dpm.service.Evaluation;
import com.elevenst.dpm.service.Forecast;
import com.elevenst.dpm.service.InventoryChecks;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.ui.NumberField;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component("oneProductGrid")
@Scope("prototype")
public class OneProductGrid{
    @Autowired
    private Evaluation evaluation;

    @Autowired
    private Forecast forecast;

    private String GRID_WIDTH = "1440px";
    private String GRID_HEIGHT = "450px";
    private String IMG_WIDTH = "160px";
    private String IMG_HEIGHT = "160px";
    private String PRDINFO_PANEL_WIDTH = "250px";
    private String CHART_PANEL_WIDTH = "940px";
    private String FORECAST_PANEL_WIDTH = "250px";
    private String CHART_WIDTH = "930px";
    private String CHART_HEIGHT = "430px";
    private String BUTTON_WIDTH = "100px";
    private String MESSAGE_HEIGHT = "80px";

    private GridLayout oneProductGrid;
    private TextArea smapeArea;
    private Image prdImage;
    private LineChartConfig lineConfig;
    private ChartJs chart;
    private NumberField prdNoTf;
    private NumberField priceTf;
    private CheckBoxGroup<String> inventories;

    GridLayout getOneProductGrid() {
        return oneProductGrid;
    }

    public OneProductGrid(){
        oneProductGrid = new GridLayout(5, 4);
        oneProductGrid.setWidth(GRID_WIDTH);
        oneProductGrid.addComponent(getPrdInfoPanel(), 0, 0, 0, 3);
        oneProductGrid.addComponent(getChartPanel(), 1, 0, 3, 3);
        oneProductGrid.addComponent(getForecastPanel(), 4, 0, 4, 3);
    }

    public void setProductNo(String prdNo){
        this.prdNoTf.setValue(prdNo);
    }

    private Panel getPrdInfoPanel(){
        // Product Information Panel
        Panel prdInfoPanel = new Panel("Product Infomation");
        prdInfoPanel.setWidth(PRDINFO_PANEL_WIDTH);
        prdInfoPanel.setHeight(GRID_HEIGHT);

        VerticalLayout prdInfoLayout = new VerticalLayout();
        prdNoTf = new NumberField("PrdNo");
        prdNoTf.setIcon(VaadinIcons.BOOK);
        prdNoTf.setDescription("상품 번호를 입력하세요");
        prdNoTf.addStyleName(ValoTheme.TEXTFIELD_TINY);
        prdNoTf.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        prdNoTf.setDecimalAllowed(true);
        prdNoTf.setGroupingUsed(false);
        prdNoTf.setWidth("100%");

        prdImage = new Image();
        prdImage.setWidth(IMG_WIDTH);
        prdImage.setHeight(IMG_HEIGHT);
        prdImage.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        prdImage.addStyleName(ValoTheme.LABEL_FAILURE);
        //prdImage.setSource(new ExternalResource("http://i.011st.com/ex_t/R/160x160/1/85/1/src/pd/18/4/8/1/2/0/5/WvoPU/2151481205_L300.jpg"));
        prdImage.setSource(new ExternalResource("https://yt3.ggpht.com/a-/AN66SAzhCf4nbx9cShmzCOzxbzNvBBJDt4PUu5N6_A=s900-mo-c-c0xffffffff-rj-k-no"));


        HorizontalLayout buttonLayout = new HorizontalLayout();
        Button checkBt = new Button("Check");
        checkBt.setStyleName(ValoTheme.BUTTON_TINY);
        checkBt.setIcon(VaadinIcons.CHECK);
        checkBt.setWidth(BUTTON_WIDTH);
        checkBt.addClickListener(e -> prdImage.setSource(new ExternalResource(evaluation.getPrdImage(prdNoTf.getValue()))));

        Button evaluateBt = new Button("Evaluate");
        evaluateBt.setStyleName(ValoTheme.BUTTON_TINY);
        evaluateBt.setIcon(VaadinIcons.QUESTION);
        evaluateBt.setWidth(BUTTON_WIDTH);
        evaluateBt.addClickListener(e -> {
            String prdNo = prdNoTf.getValue();
            if(prdNo != null && prdNo.length() > 0) {
                Map<ForecastModel, List<ForecastResult>> lastWeekForecast = forecast.forecastLastWeek(prdNo);
                if(lastWeekForecast != null && lastWeekForecast.size() > 0) {
                    List<SMAPEResult> evaluationResult = evaluation.evaluateLastWeek(prdNo, lastWeekForecast);
                    log.info(evaluationResult + "");
                    lineConfig.data().clear();
                    for(SMAPEResult smapeResult : evaluationResult){
                        LineDataset dataSet = new LineDataset();
                        dataSet.label(smapeResult.getForecastModel().getModelName());
                        dataSet.fill(false);

                        for(ForecastResult fr : smapeResult.getForecastResults()){
                            dataSet.addLabeledData(fr.getOrdDt() + "", (double)fr.getOrdQty());
                        }

                        dataSet.borderColor(ColorUtils.randomColor(0.3));
                        dataSet.backgroundColor(ColorUtils.randomColor(0.5));

                        lineConfig.data().addDataset(dataSet);

                        smapeArea.setValue(smapeArea.getValue() + "\n"
                                + Calendar.getInstance().getTime() + "\n"
                                + "[Model Name]:"
                                + smapeResult.getForecastModel().getModelName() + "\n" + "[SMAPE]:" + smapeResult.getSmape() + "\n");
                        smapeArea.setCursorPosition(smapeArea.getValue().length());
                    }

                    lineConfig.data().labelsAsList(this.labelsLastWeek());
                    lineConfig.data().addDataset(getActualDataSet(prdNo));
                    lineConfig.options().legend().display(true);
                    chart.update();
                }
            }
        });
        buttonLayout.addComponents(checkBt, evaluateBt);

        smapeArea = new TextArea("SMAPE : ");
        smapeArea.addStyleName(ValoTheme.LABEL_BOLD);
        smapeArea.addStyleName(ValoTheme.LABEL_SUCCESS);
        smapeArea.setWordWrap(true);
        smapeArea.setWidth("100%");
        smapeArea.setHeight(MESSAGE_HEIGHT);
        smapeArea.setReadOnly(true);
        smapeArea.addStyleName("dynamicprice");

        prdInfoLayout.addComponents(prdNoTf, prdImage, buttonLayout, smapeArea);
        prdInfoLayout.setComponentAlignment(prdImage, Alignment.MIDDLE_CENTER);
        prdInfoPanel.setContent(prdInfoLayout);
        return prdInfoPanel;
    }

    private LineDataset getActualDataSet(String prdNo){
        LineDataset dataSet = new LineDataset();
        dataSet.label("Actual Sales").fill(true);
        List<DailyOrder> lastWeekOrder = evaluation.getLastWeekDailyOrder(prdNo);
        for(DailyOrder dailyOrder : lastWeekOrder){
            dataSet.addLabeledData(dailyOrder.getOrdDt() + "", (double)dailyOrder.getOrdQty());
        }
        return dataSet;
    }

    private List<String> labelsNextWeek(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

        List<String> labels = new ArrayList<>();
        for(int i = 0; i < 7; i++){
            labels.add(sdformat.format(cal.getTime()));
            cal.add(Calendar.DATE, 1);
        }

        return labels;
    }

    private List<String> labelsLastWeek(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
        cal.add(Calendar.DATE, -7);

        List<String> labels = new ArrayList<>();
        for(int i = 0; i < 7; i++){
            labels.add(sdformat.format(cal.getTime()));
            cal.add(Calendar.DATE, 1);
        }

        return labels;
    }

    private Panel getChartPanel(){
        // chart
        Panel chartPanel = new Panel();
        lineConfig = new LineChartConfig();

        lineConfig.data()
                .labelsAsList(this.labelsLastWeek())
                .addDataset(new LineDataset().label("Actual Sales").fill(false))
                .and()
                .options()
                .responsive(true)
                .title()
                .display(true)
                .text("Sales Quantity")
                .and()
                .tooltips()
                .mode(InteractionMode.INDEX)
                .intersect(false)
                .and()
                .hover()
                .mode(InteractionMode.NEAREST)
                .intersect(true)
                .and()
                .scales()
                .add(Axis.X, new CategoryScale()
                        .display(true)
                        .scaleLabel()
                        .display(true)
                        .labelString("Date")
                        .and()
                        .position(Position.BOTTOM))
                .add(Axis.Y, new LinearScale()
                        .display(true)
                        .scaleLabel()
                        .display(true)
                        .labelString("Quantity")
                        .and()
                        .ticks()
                        .suggestedMin(0)
                        .and()
                        .position(Position.LEFT))
                .and()
                .done();

        // add random data for demo
        List<String> labels = lineConfig.data().getLabels();
        for (Dataset<?, ?> ds : lineConfig.data().getDatasets()) {
            LineDataset lds = (LineDataset) ds;
            List<Double> data = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                data.add((double) Math.round(Math.random() * 100));
            }
            lds.dataAsList(data);
            lds.borderColor(ColorUtils.randomColor(0.3));
            lds.backgroundColor(ColorUtils.randomColor(0.5));
        }

        chart = new ChartJs(lineConfig);
        chart.setJsLoggingEnabled(true);
        chart.setWidth(CHART_WIDTH);
        chart.setHeight(CHART_HEIGHT);

        chartPanel.setContent(chart);
        chartPanel.setHeight(GRID_HEIGHT);
        chartPanel.setWidth(CHART_PANEL_WIDTH);
        return chartPanel;
    }

    private Panel getForecastPanel(){
        // Forcast Input Panel
        Panel forecastPanel = new Panel("Input for forecast");
        forecastPanel.setWidth(FORECAST_PANEL_WIDTH);
        forecastPanel.setHeight(GRID_HEIGHT);
        VerticalLayout v2 = new VerticalLayout();

        priceTf = new NumberField("FinalDscPrc");
        priceTf.setIcon(VaadinIcons.MONEY);
        priceTf.addStyleName(ValoTheme.TEXTFIELD_TINY);
        priceTf.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        priceTf.setSizeFull();
        priceTf.setPlaceholder("가격을 입력하세요(예: 15000)");
        priceTf.setDescription("가격을 입력하지 않으시면, 현재 할인 모음가로 세팅 됩니다.");
        priceTf.setDecimalAllowed(true);
        priceTf.setGroupingUsed(true);

        inventories = new CheckBoxGroup<>("홈탭 영역 선택");
        inventories.setItems("상단 빌보드", "띠배너", "기획전1", "긴급공수", "쇼킹딜 상품", "쇼킹딜 띠배너", "중단 빌보드", "동영상", "히트상품", "MD추천");
        inventories.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        inventories.select("일반");

        Button forecastBt = new Button("Forecast");
        forecastBt.setStyleName(ValoTheme.BUTTON_TINY);
        forecastBt.setIcon(VaadinIcons.SUBSCRIPT);
        forecastBt.setWidth(BUTTON_WIDTH);
        forecastBt.addClickListener(e -> {
            String prdNo = prdNoTf.getValue();
            if(prdNo != null && prdNo.length() > 0) {
                String priceStr = priceTf.getValue();
                long price;
                if(priceStr == null || priceStr.length() == 0){
                    price = evaluation.getFinalDscPrc(prdNo);
                    priceTf.setValue(price + "");
                }else{
                    price = Long.parseLong(priceStr);
                }

                Set<String> selectedInentories = inventories.getSelectedItems();
                InventoryChecks inventoryChecks = new InventoryChecks(selectedInentories);
                Map<ForecastModel, List<ForecastResult>> nextWeekForecast = forecast.forecastNextWeek(prdNo, price, inventoryChecks);
                if(nextWeekForecast != null && nextWeekForecast.size() > 0){
                    log.info(nextWeekForecast + "");
                    lineConfig.data().clear();

                    for(ForecastModel forecastModel : nextWeekForecast.keySet()){
                        LineDataset dataSet = new LineDataset();
                        dataSet.label(forecastModel.getModelName());
                        dataSet.fill(false);

                        for(ForecastResult forecastResult : nextWeekForecast.get(forecastModel)){
                            dataSet.addLabeledData(forecastResult.getOrdDt() + "", (double)forecastResult.getOrdQty());
                        }

                        dataSet.borderColor(ColorUtils.randomColor(0.3));
                        dataSet.backgroundColor(ColorUtils.randomColor(0.5));

                        lineConfig.data().addDataset(dataSet);

                        smapeArea.setValue(smapeArea.getValue() + "\n"
                                + Calendar.getInstance().getTime() + "\n"
                                + "[Model Name]:"
                                + forecastModel.getModelName() + "\n"
                                + "Forecasting for next week is done.\n");
                        smapeArea.setCursorPosition(smapeArea.getValue().length());
                    }

                    lineConfig.data().labelsAsList(this.labelsNextWeek());
                    lineConfig.options().legend().display(true);
                    chart.update();
                }
            }
        });

        v2.addComponents(priceTf, inventories, forecastBt);
        forecastPanel.setContent(v2);
        return forecastPanel;
    }
}