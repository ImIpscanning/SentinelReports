package com.imipscanning.sentinelreports.common;

import com.imipscanning.sentinelreports.common.util.TimeParser;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeParserTest {
    @Test
    void parsesCompositeDurations() {
        assertThat(TimeParser.parse("1h 30m")).isEqualTo(Duration.ofMinutes(90));
        assertThat(TimeParser.parse("2m")).isEqualTo(Duration.ofMinutes(2));
        assertThat(TimeParser.parse("45")).isEqualTo(Duration.ofSeconds(45));
    }

    @Test
    void rejectsInvalidDurations() {
        assertThatThrownBy(() -> TimeParser.parse("1x")).isInstanceOf(IllegalArgumentException.class);
    }
}
