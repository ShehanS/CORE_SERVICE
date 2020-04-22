package service;

import org.joda.time.DateTimeZone;
import play.data.format.Formats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


import java.sql.Timestamp;

public class Clock {
    private static String CURRENT_DATE;
    private static String DAY_BEFORE_DATE;
    private DateTimeFormatter dtf;
    private LocalDateTime now;
    private LocalDateTime dayBefore;
    private Timestamp ts;


    public Clock() {
        dtf = DateTimeFormatter.ofPattern("M/d/yyyy");
        now = LocalDateTime.now();
    }

    public String getCurrentDate() {
        return CURRENT_DATE = (dtf.format(now));
    }


    public String getDayBeforeDate() {
        dayBefore = now.minusDays(1);
        return DAY_BEFORE_DATE = (dtf.format(dayBefore));
    }

    public Long getCurrentDateAsEpoch() {
        return ts.getTime();
    }


}
