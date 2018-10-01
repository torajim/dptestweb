package com.elevenst.dpm.fcmodel;

import com.elevenst.dpm.service.InventoryChecks;

import java.util.List;

public interface ForecastModel {
    String getModelName();
    default String getModelDescription(){
        return "Default Model Description";
    }
    // RegisterForecast d0 to d6 with given finalDscPrc
    List<ForecastResult> forecastNextWeek(String prdNo, long finalDscPrc, InventoryChecks inventoryChecks);
    // RegisterForecast d-7 to d-1 with actual price
    List<ForecastResult> forecastLastWeek(String prdNo);
}