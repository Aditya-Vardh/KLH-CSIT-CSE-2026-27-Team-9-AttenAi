package com.attendai.ai.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Rule-based NLP intent extractor for attendance-related natural-language messages.
 * Designed to work offline without an external AI API.
 * Can be swapped for an LLM-based implementation by replacing the extractIntent/extractTime methods.
 */
@Service
public class NlpService {

    public enum Intent { LATE_ARRIVAL, ABSENT_TODAY, EARLY_LEAVE, PRESENT, UNKNOWN }

    private static final Pattern TIME_PATTERN =
            Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern LATE_MINUTES =
            Pattern.compile("(\\d+)\\s*(?:minute|min)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LATE_HOURS =
            Pattern.compile("(\\d+)\\s*(?:hour|hr)", Pattern.CASE_INSENSITIVE);

    public Intent extractIntent(String message) {
        String m = message.toLowerCase();
        if (m.matches(".*(won't be|will not be|cannot come|can't come|absent|not coming).*")) return Intent.ABSENT_TODAY;
        if (m.matches(".*(late|delay|stuck|traffic|running late|coming late|be late|arrive late).*")) return Intent.LATE_ARRIVAL;
        if (m.matches(".*(leave early|early leave|leaving early).*")) return Intent.EARLY_LEAVE;
        if (m.matches(".*(on my way|arrived|checking in|present).*")) return Intent.PRESENT;
        return Intent.UNKNOWN;
    }

    /**
     * Try to extract an expected arrival time from the message.
     * e.g. "I'll be 30 minutes late" → now + 30 min
     *      "Coming at 10:30"          → 10:30
     */
    public LocalTime extractExpectedArrival(String message) {
        // Try explicit "X minutes late"
        Matcher minMatcher = LATE_MINUTES.matcher(message);
        if (minMatcher.find()) {
            int mins = Integer.parseInt(minMatcher.group(1));
            return LocalTime.of(9, 0).plusMinutes(mins);  // baseline 9:00 AM
        }
        // Try explicit "X hours late"
        Matcher hrMatcher = LATE_HOURS.matcher(message);
        if (hrMatcher.find()) {
            int hrs = Integer.parseInt(hrMatcher.group(1));
            return LocalTime.of(9, 0).plusHours(hrs);
        }
        // Try "at HH:MM" or "by HH:MM"
        Matcher timeMatcher = TIME_PATTERN.matcher(message);
        if (timeMatcher.find()) {
            try {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = timeMatcher.group(2) != null ? Integer.parseInt(timeMatcher.group(2)) : 0;
                String ampm = timeMatcher.group(3);
                if ("pm".equalsIgnoreCase(ampm) && hour < 12) hour += 12;
                if ("am".equalsIgnoreCase(ampm) && hour == 12) hour = 0;
                return LocalTime.of(Math.min(hour, 23), Math.min(minute, 59));
            } catch (Exception ignored) {}
        }
        // Default: 30 minutes late
        return LocalTime.of(9, 30);
    }

    /** Compute minutes between standard start (9:00) and expected arrival. */
    public int computeLateMinutes(LocalTime expectedArrival) {
        LocalTime standard = LocalTime.of(9, 0);
        if (expectedArrival.isAfter(standard)) {
            return (int) java.time.temporal.ChronoUnit.MINUTES.between(standard, expectedArrival);
        }
        return 0;
    }

    /** Identify the attendance date — default today; "tomorrow" not supported. */
    public LocalDate extractDate(String message) {
        return LocalDate.now();
    }
}
