package com.elevenst.dpm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Created by 1003724 on 2017-07-12.
 */
public class PidUtil {
    private static final Logger log = LoggerFactory.getLogger(PidUtil.class);

    public static void savePid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pidNumber = name.substring(0, name.indexOf("@"));

        File o = new File("/tmp", "dynamicPrice.pid");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(o));
            bw.write(pidNumber);
            bw.flush();
        } catch (IOException e) {
            log.error("PID save error: \r\n{}", e.toString());
        } finally {
            if (bw != null) try {bw.close(); } catch (IOException e) {}
        }
    }
}