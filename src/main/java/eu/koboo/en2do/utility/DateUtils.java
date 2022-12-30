package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class DateUtils {

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
