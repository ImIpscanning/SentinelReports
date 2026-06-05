package com.imipscanning.sentinelreports.common.model;

public record ReportQuery(
        ReportFilter filter,
        int page,
        int pageSize,
        String sortBy,
        boolean descending
) {
    public ReportQuery {
        filter = filter == null ? ReportFilter.empty() : filter;
        page = Math.max(1, page);
        pageSize = Math.max(1, Math.min(100, pageSize));
        sortBy = sortBy == null || sortBy.isBlank() ? "created_at" : sortBy;
    }

    public int offset() {
        return (page - 1) * pageSize;
    }

    public static ReportQuery firstPage(ReportFilter filter) {
        return new ReportQuery(filter, 1, 10, "created_at", true);
    }
}
