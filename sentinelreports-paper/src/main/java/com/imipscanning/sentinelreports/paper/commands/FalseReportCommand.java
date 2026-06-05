package com.imipscanning.sentinelreports.paper.commands;

import com.imipscanning.sentinelreports.common.model.ReportStatus;

import java.util.List;

public final class FalseReportCommand extends CloseReportCommand {
    public FalseReportCommand() {
        super("false", "Marca un reporte como falso.", "sentinelreports.staff.false", ReportStatus.FALSE_REPORT,
                "false <id> <reason>", List.of("/reports false 15 Reporte malicioso"));
    }
}
