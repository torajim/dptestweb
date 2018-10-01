package com.elevenst.dpm.service;

import com.elevenst.dpm.fcmodel.ForecastModel;
import com.elevenst.dpm.fcmodel.ForecastResult;
import com.elevenst.dpm.repository.DailyOrder;
import com.elevenst.dpm.repository.EvaluationMapper;
import com.elevenst.dpm.repository.SMAPEResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.*;

@Component
@Slf4j
public class Evaluation {
    @Autowired
    private EvaluationMapper mapper;

    public String getPrdImage(String prdNo){
        String result = "http://i.011st.com/ex_t/R/160x160/1/85/1/src/pd/18/4/8/1/2/0/5/WvoPU/2151481205_L300.jpg";
        String prdImage = mapper.selectImgUrlByPrdNo(prdNo);
        log.info("http://image.11st.co.kr/t/300" + prdImage);
        return prdImage != null && prdImage.length() > 0 ? "http://image.11st.co.kr/t/300" + prdImage : result;
    }

    public Long getFinalDscPrc(String prdNo){
        return mapper.selectFinalDscPriceByPrdNo(prdNo);
    }

    public List<DailyOrder> getLastWeekDailyOrder(String prdNo){
        return mapper.selectLastWeekSalesByPrdNo(prdNo);
    }

    // https://en.wikipedia.org/wiki/Symmetric_mean_absolute_percentage_error
    public List<SMAPEResult> evaluateLastWeek(String prdNo, Map<ForecastModel, List<ForecastResult>> lastWeekForecastResults){
        List<SMAPEResult> smapeResults = new ArrayList<>();
        List<DailyOrder> lastWeekOrders = mapper.selectLastWeekSalesByPrdNo(prdNo);
        Map<Date, Long> dailyOrderMap = new HashMap<>();
        lastWeekOrders.forEach(v -> dailyOrderMap.put(v.getOrdDt(), v.getOrdQty()));
        Iterator<ForecastModel> itr = lastWeekForecastResults.keySet().iterator();

        while(itr.hasNext()){
            float sumResidual = 0.0f;
            ForecastModel key = itr.next();
            //System.out.println(key.getModelName());
            List<ForecastResult> forecastResults = lastWeekForecastResults.get(key);
            if(forecastResults == null || forecastResults.size() == 0){
                log.error("No forecastResult:" + key);
                continue;
            }
            for(ForecastResult result : forecastResults){
                long actual = 0;
                if(result != null && result.getOrdDt() != null){
                    actual = dailyOrderMap.get(result.getOrdDt());
                }
                long forecastq = 0;
                if(result != null && result.getOrdQty() != 0){
                    forecastq = result.getOrdQty();
                }
                sumResidual += Math.abs((actual - (float)forecastq)/(actual + (float)forecastq + 1));
            }
            float smape = sumResidual / (float)forecastResults.size();
            SMAPEResult result = SMAPEResult.builder()
                    .dailySales(lastWeekOrders)
                    .forecastModel(key)
                    .forecastResults(forecastResults)
                    .smape(smape)
                    .build();
            smapeResults.add(result);
        }
        return smapeResults;
    }
}