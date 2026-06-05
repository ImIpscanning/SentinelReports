package com.imipscanning.sentinelreports.common.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.imipscanning.sentinelreports.common.model.Report;
import com.imipscanning.sentinelreports.common.model.ReportStatus;
import com.imipscanning.sentinelreports.common.util.JsonUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

public final class DiscordWebhookService {
    private final DiscordSettings settings;
    private final HttpClient httpClient;
    private final Executor executor;
    private final AtomicLong lastSend = new AtomicLong(0L);

    public DiscordWebhookService(DiscordSettings settings, Executor executor) {
        this.settings = settings;
        this.executor = executor == null ? CompletableFuture.delayedExecutor(0, java.util.concurrent.TimeUnit.MILLISECONDS) : executor;
        this.httpClient = HttpClient.newHttpClient();
    }

    public CompletableFuture<Void> sendNewReport(Report report) {
        if (!settings.enabled() || !settings.notifyNewReport()) {
            return CompletableFuture.completedFuture(null);
        }
        if (report.priority().name().equals("CRITICAL") && !settings.notifyCriticalReport()) {
            return CompletableFuture.completedFuture(null);
        }
        return send("Nuevo reporte #" + report.id(), report, 0x3498db);
    }

    public CompletableFuture<Void> sendClosed(Report report) {
        if (!settings.enabled() || !settings.notifyReportClosed()) {
            return CompletableFuture.completedFuture(null);
        }
        int color = report.status() == ReportStatus.FALSE_REPORT ? 0xe74c3c : 0x2ecc71;
        return send("Reporte " + report.status().name() + " #" + report.id(), report, color);
    }

    public CompletableFuture<Void> sendAssigned(Report report) {
        if (!settings.enabled() || !settings.notifyReportAssigned()) {
            return CompletableFuture.completedFuture(null);
        }
        return send("Reporte asignado #" + report.id(), report, 0xf1c40f);
    }

    public CompletableFuture<Void> sendTest() {
        if (!settings.enabled()) {
            return CompletableFuture.completedFuture(null);
        }
        ObjectNode payload = basePayload();
        ArrayNode embeds = payload.putArray("embeds");
        ObjectNode embed = embeds.addObject();
        embed.put("title", "SentinelReports webhook test");
        embed.put("description", "El webhook esta configurado correctamente.");
        embed.put("color", 0x2ecc71);
        embed.put("timestamp", Instant.now().toString());
        return post(payload);
    }

    private CompletableFuture<Void> send(String title, Report report, int color) {
        if (settings.webhookUrl() == null || settings.webhookUrl().isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        long now = System.currentTimeMillis();
        long previous = lastSend.get();
        if (settings.cooldownMillis() > 0 && now - previous < settings.cooldownMillis()) {
            return CompletableFuture.completedFuture(null);
        }
        lastSend.set(now);

        ObjectNode payload = basePayload();
        ArrayNode embeds = payload.putArray("embeds");
        ObjectNode embed = embeds.addObject();
        embed.put("title", title);
        embed.put("color", color);
        embed.put("timestamp", Instant.now().toString());
        ArrayNode fields = embed.putArray("fields");
        field(fields, "ID", "#" + report.id(), true);
        field(fields, "Reportado", report.targetName(), true);
        field(fields, "Reportador", report.reporterName(), true);
        field(fields, "Categoria", report.category(), true);
        field(fields, "Prioridad", report.priority().name(), true);
        field(fields, "Servidor", report.serverName(), true);
        field(fields, "Estado", report.status().name(), true);
        field(fields, "Staff asignado", report.assignedToName() == null ? "Sin asignar" : report.assignedToName(), true);
        field(fields, "Razon", report.reason(), false);
        return post(payload);
    }

    private ObjectNode basePayload() {
        ObjectNode payload = JsonUtil.mapper().createObjectNode();
        if (settings.username() != null && !settings.username().isBlank()) {
            payload.put("username", settings.username());
        }
        if (settings.avatarUrl() != null && !settings.avatarUrl().isBlank()) {
            payload.put("avatar_url", settings.avatarUrl());
        }
        return payload;
    }

    private void field(ArrayNode fields, String name, String value, boolean inline) {
        ObjectNode field = fields.addObject();
        field.put("name", name);
        field.put("value", value == null || value.isBlank() ? "-" : value);
        field.put("inline", inline);
    }

    private CompletableFuture<Void> post(ObjectNode payload) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(settings.webhookUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();
        return CompletableFuture.runAsync(() -> {
            try {
                httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception ignored) {
                // Platform code logs user-facing failures when command-triggered; background alerts stay non-fatal.
            }
        }, executor);
    }
}
