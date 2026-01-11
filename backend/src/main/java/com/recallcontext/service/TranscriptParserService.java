package com.recallcontext.service;

import com.recallcontext.exception.InvalidFilenameException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TranscriptParserService {

    // Filename pattern: YYYY-MM-DD_HHmm_MeetingType_SeriesName.txt
    private static final Pattern FILENAME_PATTERN =
            Pattern.compile("(\\d{4}-\\d{2}-\\d{2})_(\\d{4})_([\\w]+)_([\\w]+)\\.txt");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    // Valid meeting types
    private static final List<String> VALID_MEETING_TYPES = Arrays.asList(
            "OneOnOne", "Standup", "Programme", "Retro", "Governance", "Leadership",
            "Vendor", "Adhoc", "Incident", "Interview", "Review", "Dictation"
    );

    /**
     * Parse filename to extract meeting metadata
     */
    public ParsedMetadata parseFilename(String filename) {
        log.debug("Parsing filename: {}", filename);

        Matcher matcher = FILENAME_PATTERN.matcher(filename);

        if (!matcher.matches()) {
            throw new InvalidFilenameException(
                    String.format("Invalid filename format: %s. Expected format: YYYY-MM-DD_HHmm_MeetingType_SeriesName.txt", filename)
            );
        }

        try {
            String dateStr = matcher.group(1);
            String timeStr = matcher.group(2);
            String meetingType = matcher.group(3);
            String seriesName = matcher.group(4);

            // Validate meeting type
            if (!VALID_MEETING_TYPES.contains(meetingType)) {
                throw new InvalidFilenameException(
                        String.format("Invalid meeting type: %s. Valid types: %s", meetingType, String.join(", ", VALID_MEETING_TYPES))
                );
            }

            // Parse date and time
            LocalDateTime meetingDateTime = parseDateAndTime(dateStr, timeStr);

            log.info("Successfully parsed filename: date={}, type={}, series={}",
                    meetingDateTime, meetingType, seriesName);

            return ParsedMetadata.builder()
                    .meetingDate(meetingDateTime)
                    .meetingType(meetingType)
                    .seriesName(seriesName)
                    .build();

        } catch (DateTimeParseException e) {
            throw new InvalidFilenameException(
                    String.format("Invalid date/time format in filename: %s", filename), e
            );
        }
    }

    /**
     * Parse date and time strings into LocalDateTime
     */
    private LocalDateTime parseDateAndTime(String dateStr, String timeStr) {
        // Parse date (YYYY-MM-DD)
        var date = java.time.LocalDate.parse(dateStr, DATE_FORMATTER);

        // Parse time (HHmm)
        var time = LocalTime.parse(timeStr, TIME_FORMATTER);

        return LocalDateTime.of(date, time);
    }

    /**
     * Validate meeting type
     */
    public boolean isValidMeetingType(String meetingType) {
        return VALID_MEETING_TYPES.contains(meetingType);
    }

    /**
     * Get list of valid meeting types
     */
    public List<String> getValidMeetingTypes() {
        return VALID_MEETING_TYPES;
    }

    /**
     * Parsed metadata from filename
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedMetadata {
        private LocalDateTime meetingDate;
        private String meetingType;
        private String seriesName;
    }
}
