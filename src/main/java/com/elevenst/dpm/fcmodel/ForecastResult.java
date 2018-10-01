package com.elevenst.dpm.fcmodel;

import lombok.*;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ForecastResult {
    private Date ordDt;
    private long ordQty;
}
