package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportStatus;

import java.util.List;

public final class RejectReportCommand extends CloseReportCommand {
    public RejectReportCommand() {
        super("reject", "Rechaza un reporte con razon.", "sentinelreports.staff.reject", ReportStatus.REJECTED,
                "reject <id> <reason>", List.of("/reports reject 15 Sin evidencia suficiente"));
    }
}
