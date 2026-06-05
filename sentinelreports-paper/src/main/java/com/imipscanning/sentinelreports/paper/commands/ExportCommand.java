package com.imipscanning.sentinelreports.paper.commands;

import java.util.List;
import java.util.Map;

public final class ExportCommand extends AbstractSubCommand {
    public ExportCommand() {
        super("export", List.of(), "Exporta un reporte en JSON o HTML.", "sentinelreports.staff.export",
                "export <id> <json|html>", List.of("/reports export 15 html"), "admin");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        String format = context.arg(2);
        context.async(() -> context.plugin().reportService().export(id, format),
                path -> context.reply("success.exported", Map.of("path", path.toString()), "&aReporte exportado: &f" + path));
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandContext context) {
        if (context.args().length == 3) {
            return List.of("json", "html");
        }
        return List.of();
    }
}
