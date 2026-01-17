package com.example.securitycommon.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {
    
    private static final DateTimeFormatter READABLE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * Formats epoch milliseconds to a readable timestamp
     * @param epochMillis timestamp in milliseconds
     * @return formatted timestamp string (e.g., "2026-01-17 12:27:37")
     */
    public static String formatTimestamp(long epochMillis) {
        return READABLE_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    /**
     * Formats a duration in milliseconds to readable format
     * @param durationMillis duration in milliseconds
     * @return formatted duration (e.g., "1h30m45s" or "2m15s" or "500ms")
     */
    public static String formatDuration(long durationMillis) {
        if (durationMillis < 1000) {
            return durationMillis + "ms";
        }

        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        StringBuilder result = new StringBuilder();
        
        if (hours > 0) {
            result.append(hours).append("h");
        }
        if (minutes > 0) {
            result.append(minutes).append("m");
        }
        if (seconds > 0) {
            result.append(seconds).append("s");
        }
        
        // If only milliseconds left
        if (result.length() == 0) {
            result.append(durationMillis).append("ms");
        }

        return result.toString();
    }

    /**
     * Gets current timestamp as readable string
     * @return current timestamp formatted
     */
    public static String getCurrentTimestamp() {
        return formatTimestamp(System.currentTimeMillis());
    }
}
