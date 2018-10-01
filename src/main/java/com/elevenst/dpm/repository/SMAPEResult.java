package com.elevenst.dpm.repository;

import com.elevenst.dpm.fcmodel.ForecastModel;
import com.elevenst.dpm.fcmodel.ForecastResult;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SMAPEResult {
    private ForecastModel forecastModel;
    private List<ForecastResult> forecastResults;
    private List<DailyOrder> dailySales;
    private float smape;
}