package com.elevenst.dpm.service;

import com.elevenst.dpm.fcmodel.ForecastModel;
import com.elevenst.dpm.fcmodel.ForecastResult;
import com.elevenst.dpm.fcmodel.RegisterForecast;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class Forecast {

    @Autowired
    private ApplicationContext cx;

    public Map<ForecastModel, List<ForecastResult>> forecastNextWeek(String prdNo, long finalDscPrc, InventoryChecks inventoryChecks){
        Map<String, Object> models = cx.getBeansWithAnnotation(RegisterForecast.class);
        Iterator<String> itr = models.keySet().iterator();
        Map<ForecastModel, List<ForecastResult>> result = new HashMap<>();
        while(itr.hasNext()){
            String key = itr.next();
            ForecastModel model = (ForecastModel)models.get(key);
            RegisterForecast annotation = cx.findAnnotationOnBean(key, RegisterForecast.class);
            log.info(model.getModelName());
            List<ForecastResult> nextWeek = model.forecastNextWeek(prdNo, finalDscPrc, inventoryChecks);
            if(nextWeek != null && nextWeek.size() > 0) {
                result.put(model, nextWeek);
            }
        }
        return result;
    }

    public Map<ForecastModel, List<ForecastResult>> forecastLastWeek(String prdNo){
        Map<String, Object> models = cx.getBeansWithAnnotation(RegisterForecast.class);
        Iterator<String> itr = models.keySet().iterator();
        Map<ForecastModel, List<ForecastResult>> result = new HashMap<>();
        while(itr.hasNext()){
            String key = itr.next();
            ForecastModel model = (ForecastModel)models.get(key);
            RegisterForecast annotation = cx.findAnnotationOnBean(key, RegisterForecast.class);
            log.info(model.getModelName());
            List<ForecastResult> lastWeek = model.forecastLastWeek(prdNo);
            if(lastWeek != null && lastWeek.size() > 0) {
                result.put(model, lastWeek);
            }
        }
        return result;
    }
}