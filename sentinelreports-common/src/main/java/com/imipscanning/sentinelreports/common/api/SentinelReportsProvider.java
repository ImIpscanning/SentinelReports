package com.imipscanning.sentinelreports.common.api;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class SentinelReportsProvider {
    private static final AtomicReference<SentinelReportsAPI> API = new AtomicReference<>();

    private SentinelReportsProvider() {
    }

    public static SentinelReportsAPI get() {
        SentinelReportsAPI api = API.get();
        if (api == null) {
            throw new IllegalStateException("SentinelReports API is not available");
        }
        return api;
    }

    public static Optional<SentinelReportsAPI> getOptional() {
        return Optional.ofNullable(API.get());
    }

    public static void register(SentinelReportsAPI api) {
        API.set(api);
    }

    public static void unregister(SentinelReportsAPI api) {
        API.compareAndSet(api, null);
    }
}
