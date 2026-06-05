package com.imipscanning.sentinelreports.common.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class TimeFormatter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeFormatter() {
    }

    public static String compact(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return "0s";
        }
        long seconds = duration.getSeconds();
        long days = seconds / 86_400;
        seconds %= 86_400;
        long hours = seconds / 3_600;
        seconds %= 3_600;
        long minutes = seconds / 60;
        seconds %= 60;
        List<String> parts = new ArrayList<>();
        if (days > 0) parts.add(days + "d");
        if (hours > 0) parts.add(hours + "h");
        if (minutes > 0) parts.add(minutes + "m");
        if (seconds > 0 || parts.isEmpty()) parts.add(seconds + "s");
        return String.join(" ", parts);
    }

    public static String since(long epochMillis) {
        return compact(Duration.between(Instant.ofEpochMilli(epochMillis), Instant.now()));
    }

    public static String dateTime(long epochMillis, ZoneId zoneId) {
        return DATE_FORMAT.withZone(zoneId == null ? ZoneId.systemDefault() : zoneId).format(Instant.ofEpochMilli(epochMillis));
    }
}
