package com.imipscanning.sentinelreports.paper.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.Map;

public final class TeleportCommand extends AbstractSubCommand {
    public TeleportCommand() {
        super("teleport", List.of("tp"), "Teletransporta al lugar guardado del reporte.", "sentinelreports.staff.teleport",
                "teleport <id>", List.of("/reports teleport 15"), "staff");
    }

    @Override
    public CommandResult execute(CommandContext context) {
        long id = requireId(context, 1);
        var player = context.player();
        context.async(() -> context.plugin().reportService().getReportById(id).orElse(null), report -> {
            if (report == null || report.world() == null || report.x() == null || report.y() == null || report.z() == null) {
                context.reply("errors.invalid_report", Map.of(), "&cEse reporte no tiene ubicacion guardada.");
                return;
            }
            World world = Bukkit.getWorld(report.world());
            if (world == null) {
                context.reply("errors.invalid_world", Map.of("world", report.world()), "&cMundo no cargado.");
                return;
            }
            context.plugin().scheduler().runEntity(player, () -> player.teleport(new Location(world, report.x(), report.y(), report.z())));
        });
        return CommandResult.SUCCESS;
    }
}
