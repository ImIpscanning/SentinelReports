package com.imipscanning.sentinelreports.common.service;

import com.imipscanning.sentinelreports.common.model.ReportHubButton;
import com.imipscanning.sentinelreports.common.model.ReportHubCategory;

import java.util.List;

public final class ReportHubService {
    private final List<ReportHubCategory> categories;

    public ReportHubService() {
        this.categories = List.of(new ReportHubCategory("staff", "Staff", List.of(
                new ReportHubButton("open", "Abiertos", "Ver reportes abiertos", "/reports list open", "sentinelreports.staff.list", false),
                new ReportHubButton("assigned", "Asignados", "Ver tus reportes asignados", "/reports list assigned", "sentinelreports.staff.list", false),
                new ReportHubButton("history", "Historial", "Buscar historial de jugador", "/reports history ", "sentinelreports.staff.history", true),
                new ReportHubButton("stats", "Stats", "Ver estadisticas del sistema", "/reports stats", "sentinelreports.staff.stats", false),
                new ReportHubButton("discord", "Discord", "Probar webhook", "/reports discord test", "sentinelreports.staff.discord", false),
                new ReportHubButton("reload", "Reload", "Recargar configuracion", "/reports reload", "sentinelreports.staff.reload", false)
        )));
    }

    public List<ReportHubButton> staffButtons() {
        return categories.stream()
                .filter(category -> category.id().equals("staff"))
                .findFirst()
                .map(ReportHubCategory::buttons)
                .orElse(List.of());
    }
}
