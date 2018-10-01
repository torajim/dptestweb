package com.elevenst.dpm.repository;

import static com.elevenst.dpm.config.BIDBConfig.BIDB;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
@BIDB
public interface EvaluationMapper {
    List<DailyOrder> selectLastWeekSalesByPrdNo(String prdNo);
    String selectImgUrlByPrdNo(String prdNo);
    Long selectFinalDscPriceByPrdNo(String prdNo);
}