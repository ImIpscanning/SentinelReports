package com.imipscanning.sentinelreports.paper.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class ReportInventoryHolder implements InventoryHolder {
    private final Type type;
    private final UUID targetUuid;
    private final String targetName;
    private final long reportId;
    private Inventory inventory;

    public ReportInventoryHolder(Type type, UUID targetUuid, String targetName, long reportId) {
        this.type = type;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.reportId = reportId;
    }

    public Type type() {
        return type;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public String targetName() {
        return targetName;
    }

    public long reportId() {
        return reportId;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public enum Type {
        PLAYER_MAIN,
        CATEGORY,
        STAFF_MAIN,
        OPEN_REPORTS,
        REPORT_DETAIL
    }
}
