package com.elevenst.dpm.repository;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RecurrentRFFeatureMatrixV1 {
    private String mdt;
    private String dow;
    private int holiday;
    private int dist_next_wkdy;
    private int evnt_mw_rthit;
    private int evnt_mw_shckdeal;
    private int evnt_mw_mvbanner;
    private int evnt_mw_mdrecom;
    private long mod_dsc_prc;
    private long mod_sel_prc;
    private long qty_sum_d1;
    private long qty_sum_d2;
    private long qty_sum_d3;
    private long qty_sum_d4;
    private long qty_sum_d5;
    private long qty_sum_d6;
    private long qty_sum_d7;
    private long qty_sum_d8;
    private long qty_sum_d9;
    private long qty_sum_d10;
    private long qty_sum_d11;
    private long qty_sum_d12;
    private long qty_sum_d13;
    private long qty_sum_d14;
    private double qty_sum_avg;
}
