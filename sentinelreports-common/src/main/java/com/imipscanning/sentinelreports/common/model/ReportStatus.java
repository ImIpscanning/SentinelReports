package com.imipscanning.sentinelreports.common.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum ReportStatus {
    OPEN,
    ASSIGNED,
    REVIEWING,
    WAITING_EVIDENCE,
    RESOLVED,
    REJECTED,
    FALSE_REPORT,
    CLOSED;

    private static final Set<ReportStatus> TERMINAL = Set.of(RESOLVED, REJECTED, FALSE_REPORT, CLOSED);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    public boolean canTransitionTo(ReportStatus next) {
        if (this == next) {
            return true;
        }
        if (isTerminal()) {
            return false;
        }
        return switch (this) {
            case OPEN -> Set.of(ASSIGNED, REVIEWING, WAITING_EVIDENCE, RESOLVED, REJECTED, FALSE_REPORT, CLOSED).contains(next);
            case ASSIGNED -> Set.of(REVIEWING, WAITING_EVIDENCE, RESOLVED, REJECTED, FALSE_REPORT, CLOSED, OPEN).contains(next);
            case REVIEWING -> Set.of(WAITING_EVIDENCE, RESOLVED, REJECTED, FALSE_REPORT, CLOSED, ASSIGNED).contains(next);
            case WAITING_EVIDENCE -> Set.of(REVIEWING, RESOLVED, REJECTED, FALSE_REPORT, CLOSED).contains(next);
            default -> false;
        };
    }

    public static Optional<ReportStatus> parse(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = input.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(values()).filter(status -> status.name().equals(normalized)).findFirst();
    }
}
