package com.imipscanning.sentinelreports.common;

import com.imipscanning.sentinelreports.common.model.ReportPriority;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusAndPriorityTest {
    @Test
    void validatesStatusTransitions() {
        assertThat(ReportStatus.OPEN.canTransitionTo(ReportStatus.ASSIGNED)).isTrue();
        assertThat(ReportStatus.CLOSED.canTransitionTo(ReportStatus.OPEN)).isFalse();
    }

    @Test
    void parsesPriorityCaseInsensitively() {
        assertThat(ReportPriority.parse("critical")).contains(ReportPriority.CRITICAL);
    }
}
