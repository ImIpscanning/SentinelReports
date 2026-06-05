package com.imipscanning.sentinelreports.common;

import com.imipscanning.sentinelreports.common.service.CooldownService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CooldownServiceTest {
    @Test
    void tracksCooldownRemaining() {
        CooldownService service = new CooldownService();
        UUID player = UUID.randomUUID();
        service.mark("global", player, 1_000);
        assertThat(service.active("global", player)).isTrue();
        assertThat(service.remainingMillis("global", player)).isPositive();
    }
}
