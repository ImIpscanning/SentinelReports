package com.imipscanning.sentinelreports.velocity.platform;

import com.imipscanning.sentinelreports.common.util.ColorFormatter;
import com.velocitypowered.api.command.CommandSource;

public final class VelocityMessages {
    public void line(CommandSource source) {
        source.sendMessage(ColorFormatter.component("&8&m------------------------------------"));
    }

    public void send(CommandSource source, String message) {
        source.sendMessage(ColorFormatter.component(message));
    }
}
