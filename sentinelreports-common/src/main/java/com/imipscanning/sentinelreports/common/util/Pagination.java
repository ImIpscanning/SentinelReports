package com.imipscanning.sentinelreports.common.util;

import java.util.Collections;
import java.util.List;

public record Pagination<T>(List<T> items, int page, int pageSize, long totalItems) {
    public Pagination {
        items = items == null ? List.of() : List.copyOf(items);
        page = Math.max(1, page);
        pageSize = Math.max(1, pageSize);
        totalItems = Math.max(0, totalItems);
    }

    public int totalPages() {
        return Math.max(1, (int) Math.ceil(totalItems / (double) pageSize));
    }

    public boolean hasNext() {
        return page < totalPages();
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public static <T> Pagination<T> of(List<T> allItems, int page, int pageSize) {
        if (allItems == null || allItems.isEmpty()) {
            return new Pagination<>(Collections.emptyList(), page, pageSize, 0);
        }
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int from = Math.min((safePage - 1) * safeSize, allItems.size());
        int to = Math.min(from + safeSize, allItems.size());
        return new Pagination<>(allItems.subList(from, to), safePage, safeSize, allItems.size());
    }
}
