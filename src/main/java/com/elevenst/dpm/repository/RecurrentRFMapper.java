package com.elevenst.dpm.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import static com.elevenst.dpm.config.BIDBConfig.BIDB;

@Mapper
@BIDB
public interface RecurrentRFMapper {
    List<RecurrentRFFeatureMatrixV1> getThisWeekFeatureMatrix(@Param("dt") String dt, @Param("prdNo") long prdNo);
    List<RecurrentRFFeatureMatrixV1> getLastWeekFeatureMatrix(@Param("dt") String dt, @Param("prdNo") long prdNo);
}