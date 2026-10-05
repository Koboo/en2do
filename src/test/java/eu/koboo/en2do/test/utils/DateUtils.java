package eu.koboo.en2do.test.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * A utility class for working with "java.util.Date".
 */
@UtilityClass
public class DateUtils {

    public LocalDateTime dateToLocalDateTime(Date date) {
        return dateToLocalDateTime(date, ZoneId.systemDefault());
    }

    public LocalDateTime dateToLocalDateTime(Date date, TimeZone timeZone) {
        return dateToLocalDateTime(date, timeZone.toZoneId());
    }

    public LocalDateTime dateToLocalDateTime(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).toLocalDateTime();
    }

    public Date localDateTimeToDate(LocalDateTime localDateTime) {
        return localDateTimeToDate(localDateTime, ZoneId.systemDefault());
    }

    public Date localDateTimeToDate(LocalDateTime localDateTime, TimeZone timeZone) {
        return localDateTimeToDate(localDateTime, timeZone.toZoneId());
    }

    public Date localDateTimeToDate(LocalDateTime localDateTime, ZoneId zoneId) {
        return Date.from(localDateTime.atZone(zoneId)
            .toInstant());
    }

    public LocalTime dateToLocalTime(Date date) {
        return dateToLocalTime(date, ZoneId.systemDefault());
    }

    public LocalTime dateToLocalTime(Date date, TimeZone timeZone) {
        return dateToLocalTime(date, timeZone.toZoneId());
    }

    public LocalTime dateToLocalTime(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).toLocalTime();
    }

    public Date localTimeToDate(LocalTime localTime) {
        return localTimeToDate(localTime, ZoneId.systemDefault());
    }

    public Date localTimeToDate(LocalTime localTime, TimeZone timeZone) {
        return localTimeToDate(localTime, timeZone.toZoneId());
    }

    public Date localTimeToDate(LocalTime localTime, ZoneId zoneId) {
        return Date.from(localTime.atDate(LocalDate.now()).atZone(zoneId).toInstant());
    }

    public LocalDate dateToLocalDate(Date date) {
        return dateToLocalDate(date, ZoneId.systemDefault());
    }

    public LocalDate dateToLocalDate(Date date, TimeZone timeZone) {
        return dateToLocalDate(date, timeZone.toZoneId());
    }

    public LocalDate dateToLocalDate(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId).toLocalDate();
    }

    public Date localDateToDate(LocalDate localDateTime, ZoneId zoneId) {
        return Date.from(localDateTime.atStartOfDay(zoneId).toInstant());
    }

    public Date plus(Date date, long time, TimeUnit unit) {
        long millis = date.getTime();
        millis += unit.toMillis(time);
        return new Date(millis);
    }

    public Date createPlus(long time, TimeUnit unit) {
        return plus(new Date(), time, unit);
    }

    public Date plusMillis(Date date, long millis) {
        return plus(date, millis, TimeUnit.MILLISECONDS);
    }

    public Date plusSeconds(Date date, long seconds) {
        return plus(date, seconds, TimeUnit.SECONDS);
    }

    public Date plusMinutes(Date date, long minutes) {
        return plus(date, minutes, TimeUnit.MINUTES);
    }

    public Date plusHours(Date date, long hours) {
        return plus(date, hours, TimeUnit.HOURS);
    }

    public Date plusDays(Date date, long days) {
        return plus(date, days, TimeUnit.DAYS);
    }

    public Date plusWeeks(Date date, long weeks) {
        return plus(date, weeks * 7, TimeUnit.DAYS);
    }

    public Date minus(Date date, long time, TimeUnit unit) {
        long millis = date.getTime();
        millis -= unit.toMillis(time);
        return new Date(millis);
    }

    public Date createMinus(long time, TimeUnit unit) {
        return minus(new Date(), time, unit);
    }

    public Date minusMillis(Date date, long millis) {
        return minus(date, millis, TimeUnit.MILLISECONDS);
    }

    public Date minusSeconds(Date date, long seconds) {
        return minus(date, seconds, TimeUnit.SECONDS);
    }

    public Date minusMinutes(Date date, long minutes) {
        return minus(date, minutes, TimeUnit.MINUTES);
    }

    public Date minusHours(Date date, long hours) {
        return minus(date, hours, TimeUnit.HOURS);
    }

    public Date minusDays(Date date, long days) {
        return minus(date, days, TimeUnit.DAYS);
    }

    public Date minusWeeks(Date date, long weeks) {
        return minus(date, weeks * 7, TimeUnit.DAYS);
    }
}
