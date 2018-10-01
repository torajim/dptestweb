package com.elevenst.dpm.repository;

import lombok.*;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DailyOrder {
    private Date ordDt;
    private long ordQty;
}