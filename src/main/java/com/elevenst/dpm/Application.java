package com.elevenst.dpm;

import com.elevenst.dpm.fcmodel.ForecastModel;
import com.elevenst.dpm.fcmodel.ForecastResult;
import com.elevenst.dpm.repository.SMAPEResult;
import com.elevenst.dpm.service.Evaluation;
import com.elevenst.dpm.service.Forecast;
import com.elevenst.dpm.service.InventoryChecks;
import com.elevenst.dpm.util.PidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootApplication
@EnableAutoConfiguration
public class Application {

    private static Forecast forecast;
    private static Evaluation evaluation;

    @Autowired
    public Application(Forecast forecast, Evaluation evaluation) {
        Application.forecast = forecast;
        Application.evaluation = evaluation;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
        PidUtil.savePid();
        /*
        Map<ForecastModel, List<ForecastResult>> results = forecast.forecastNextWeek("17055815", 18800, new InventoryChecks(InventoryChecks.INVENTORY.NORMAL));
        Iterator<Map.Entry<ForecastModel, List<ForecastResult>>> iter = results.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<ForecastModel, List<ForecastResult>> result = iter.next();
            System.out.println(result.getKey().getModelName());
            for (ForecastResult r : result.getValue()) {
                System.out.println(r.getOrdDt() + "\t" + r.getOrdQty());
            }

        }*/
        //List<SMAPEResult> wapeResults = evaluation.evaluateLastWeek("2147484821", results);
        //wapeResults.forEach(v -> log.info(v + ""));
    }
}