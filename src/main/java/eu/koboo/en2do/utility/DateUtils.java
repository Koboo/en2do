package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class DateUtils {


    public LocalDateTime toLocalDateTime(Date date) {
        return toLocalDateTime(date, ZoneId.systemDefault());
    }

    public LocalDateTime toLocalDateTime(Date date, TimeZone timeZone) {
        return toLocalDateTime(date, timeZone.toZoneId());
    }

    public LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        return date.toInstant()
                .atZone(zoneId)
                .toLocalDateTime();
    }

    public Date toDate(LocalDateTime localDateTime) {
        return toDate(localDateTime, ZoneId.systemDefault());
    }

    public Date toDate(LocalDateTime localDateTime, TimeZone timeZone) {
        return toDate(localDateTime, timeZone.toZoneId());
    }

    public Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
        return Date.from(localDateTime.atZone(zoneId)
                .toInstant());
    }

    public Date plus(Date date, long time, TimeUnit unit) {
        long millis = date.getTime();
        millis += unit.toMillis(time);
        return new Date(millis);
    }

    public Date createPlus(long time, TimeUnit unit) {
        return plus(new Date(), time, unit);
    }

    public Date plusMillis(Date date, long time) {
        return plus(date, time, TimeUnit.MILLISECONDS);
    }

    public Date plusSeconds(Date date, long time) {
        return plus(date, time, TimeUnit.SECONDS);
    }

    public Date plusMinutes(Date date, long time) {
        return plus(date, time, TimeUnit.MINUTES);
    }

    public Date plusHours(Date date, long time) {
        return plus(date, time, TimeUnit.HOURS);
    }

    public Date plusDays(Date date, long time) {
        return plus(date, time, TimeUnit.DAYS);
    }

    public Date plusWeeks(Date date, long time) {
        return plus(date, time * 7, TimeUnit.DAYS);
    }

    public Date minus(Date date, long time, TimeUnit unit) {
        long millis = date.getTime();
        millis -= unit.toMillis(time);
        return new Date(millis);
    }

    public Date createMinus(long time, TimeUnit unit) {
        return minus(new Date(), time, unit);
    }

    public Date minusMillis(Date date, long time) {
        return minus(date, time, TimeUnit.MILLISECONDS);
    }

    public Date minusSeconds(Date date, long time) {
        return minus(date, time, TimeUnit.SECONDS);
    }

    public Date minusMinutes(Date date, long time) {
        return minus(date, time, TimeUnit.MINUTES);
    }

    public Date minusHours(Date date, long time) {
        return minus(date, time, TimeUnit.HOURS);
    }

    public Date minusDays(Date date, long time) {
        return minus(date, time, TimeUnit.DAYS);
    }

    public Date minusWeeks(Date date, long time) {
        return minus(date, time * 7, TimeUnit.DAYS);
    }
}
