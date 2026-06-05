package com.imipscanning.sentinelreports.common;

import com.imipscanning.sentinelreports.common.util.PlaceholderFormatter;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceholderFormatterTest {
    @Test
    void replacesPercentPlaceholders() {
        assertThat(PlaceholderFormatter.format("Reporte #%id% contra %target%", Map.of("id", 7, "target", "Steve")))
                .isEqualTo("Reporte #7 contra Steve");
    }
}
