package com.imipscanning.sentinelreports.common.service;

public interface PermissionService<T> {
    boolean has(T subject, String permission);
}
