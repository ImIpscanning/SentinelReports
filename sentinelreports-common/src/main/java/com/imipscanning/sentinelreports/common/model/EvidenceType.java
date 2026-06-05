package com.imipscanning.sentinelreports.common.model;

import java.net.URI;
import java.util.Locale;

public enum EvidenceType {
    TEXT,
    LINK,
    SCREENSHOT_URL,
    VIDEO_URL,
    INTERNAL_NOTE,
    COORDINATES,
    SERVER,
    TIMESTAMP;

    public static EvidenceType detect(String content) {
        if (content == null) {
            return TEXT;
        }
        String value = content.trim().toLowerCase(Locale.ROOT);
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return TEXT;
        }
        URI uri = URI.create(value);
        String host = uri.getHost() == null ? "" : uri.getHost();
        String path = uri.getPath() == null ? "" : uri.getPath();
        if (host.contains("youtube.") || host.contains("youtu.be") || host.contains("twitch.") || path.endsWith(".mp4") || path.endsWith(".webm")) {
            return VIDEO_URL;
        }
        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".gif") || path.endsWith(".webp")) {
            return SCREENSHOT_URL;
        }
        return LINK;
    }
}
