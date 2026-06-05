package com.imipscanning.sentinelreports.common;

import com.imipscanning.sentinelreports.common.util.CommandArgumentParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandArgumentParserTest {
    @Test
    void parsesPositiveLongsAndJoinsArgs() {
        assertThat(CommandArgumentParser.positiveLong("42")).contains(42L);
        assertThat(CommandArgumentParser.positiveLong("-1")).isEmpty();
        assertThat(CommandArgumentParser.join(new String[]{"a", "b", "c"}, 1)).isEqualTo("b c");
    }
}
