# SentinelReports

SentinelReports is a production-oriented reports system for Minecraft networks using Velocity as proxy and Paper/Purpur backends.

It is built with Java 21, Gradle Kotlin DSL, Adventure, MiniMessage-style formatting, HikariCP, SQLite/MySQL storage, versioned migrations, Discord webhooks, GUI menus, staff workflows, anti-abuse controls and a public API.

## What It Does

- Player-to-player reports with categories and custom reasons.
- Configurable report categories, priorities and cooldowns.
- Report states: `OPEN`, `ASSIGNED`, `REVIEWING`, `WAITING_EVIDENCE`, `RESOLVED`, `REJECTED`, `FALSE_REPORT`, `CLOSED`.
- Evidence and internal staff notes.
- Claim, unclaim and assign workflow for staff.
- History by reporter and by target.
- SQLite for local/testing and MySQL/MariaDB for production.
- Discord webhook alerts.
- JSON and HTML exports.
- Paper GUI for players and staff.
- Velocity global commands and staff alerts.
- PlaceholderAPI expansion on Paper.
- Public API through `SentinelReportsProvider`.

## What It Is Not

SentinelReports is not an anticheat, anti-exploit, login/auth system or full punishment core. It can trigger external punishment commands from configuration in future integrations, but its core responsibility is report management.

## Build

```bash
gradle clean test shadowJar
```

Artifacts:

- `sentinelreports-paper/build/libs/SentinelReports-Paper-1.0.0.jar`
- `sentinelreports-velocity/build/libs/SentinelReports-Velocity-1.0.0.jar`

Java 21 is required.

## Installation

### Paper/Purpur

1. Build the project.
2. Put `SentinelReports-Paper-1.0.0.jar` in `plugins/`.
3. Start the server once.
4. Edit `plugins/SentinelReports/config.yml`, `messages.yml` and `gui.yml`.
5. Restart or run `/reports reload`.

### Velocity

1. Put `SentinelReports-Velocity-1.0.0.jar` in `plugins/`.
2. Start the proxy once.
3. Edit `plugins/sentinelreports/config.yml`.
4. Use the same MySQL/MariaDB database as the Paper servers for network-wide state.

## Database

Default mode is SQLite:

```yaml
database:
  type: sqlite
  sqlite_file: "sentinelreports.db"
```

Production networks should use MySQL/MariaDB:

```yaml
database:
  type: mysql
  mysql:
    host: "localhost"
    port: 3306
    database: "sentinelreports"
    username: "root"
    password: "change-me"
    pool_size: 10
```

Migrations are versioned under `sentinelreports-common/src/main/resources/db/migration`.

## Discord

Enable webhook alerts in `config.yml`:

```yaml
discord:
  enabled: true
  webhook_url: "https://discord.com/api/webhooks/..."
  notify_new_report: true
  notify_critical_report: true
  notify_report_closed: true
```

Tokens are never shown in chat or logs by command output.

## Player Commands

- `/report`
- `/report help`
- `/report <player>`
- `/report <player> <category> <reason>`
- `/report cancel`
- `/report status`
- `/report history`

## Staff Commands

- `/reports`, `/reports gui`, `/reports hub`
- `/reports help [page|player|staff|evidence|notes|history|admin]`
- `/reports list [open|assigned|closed|status]`
- `/reports view <id>`
- `/reports claim <id>`
- `/reports unclaim <id>`
- `/reports assign <id> <staff>`
- `/reports status <id> <status>`
- `/reports priority <id> <priority>`
- `/reports close <id> <reason>`
- `/reports reject <id> <reason>`
- `/reports false <id> <reason>`
- `/reports evidence add <id> <text/link>`
- `/reports evidence list <id>`
- `/reports evidence remove <id> <evidenceId>`
- `/reports note add <id> <note>`
- `/reports note list <id>`
- `/reports history <player>`
- `/reports against <player>`
- `/reports by <player>`
- `/reports teleport <id>`
- `/reports server <id>`
- `/reports notify <id>`
- `/reports stats [staff]`
- `/reports reload`
- `/reports discord test`
- `/reports export <id> <json|html>`

## Permissions

- `sentinelreports.admin`
- `sentinelreports.report`
- `sentinelreports.report.staff`
- `sentinelreports.staff`
- `sentinelreports.staff.list`
- `sentinelreports.staff.view`
- `sentinelreports.staff.claim`
- `sentinelreports.staff.assign`
- `sentinelreports.staff.close`
- `sentinelreports.staff.reject`
- `sentinelreports.staff.false`
- `sentinelreports.staff.note`
- `sentinelreports.staff.evidence`
- `sentinelreports.staff.history`
- `sentinelreports.staff.teleport`
- `sentinelreports.staff.stats`
- `sentinelreports.staff.reload`
- `sentinelreports.staff.discord`
- `sentinelreports.staff.export`
- `sentinelreports.bypass.cooldown`
- `sentinelreports.bypass.limit`

## GUI

Paper includes:

- Player main report menu.
- Category selection menu.
- Staff hub menu.
- Open reports menu.
- Report detail menu with claim, evidence, notes, teleport, critical and resolve actions.

All menu titles, items, slots, materials and lore are configurable in `gui.yml`.

## Anti-Abuse

Configured in `config.yml`:

```yaml
abuse_protection:
  enabled: true
  global_cooldown: "2m"
  same_target_cooldown: "10m"
  max_open_reports_per_player: 5
  block_duplicate_reports: true
  duplicate_window: "30m"
  false_report_threshold: 5
```

The system blocks self-reports, duplicate recent reports, cooldown abuse and excessive open reports unless the player has bypass permissions.

## API

```java
SentinelReportsAPI api = SentinelReportsProvider.get();

api.createReport(request);
api.closeReport(15L, ReportStatus.RESOLVED, staffUuid, "Moderator", "Handled");
api.addEvidence(15L, "https://example.com/video", staffUuid, "Moderator");
api.addNote(15L, "Checked logs", staffUuid, "Moderator");
api.getOpenReports();
```

Use `SentinelReportsProvider.getOptional()` if your plugin should stay safe when SentinelReports is not installed.

## PlaceholderAPI

Paper placeholders:

- `%sentinelreports_open_reports%`
- `%sentinelreports_assigned_reports%`
- `%sentinelreports_player_reports%`
- `%sentinelreports_player_false_reports%`

## Production Recommendations

- Use MySQL/MariaDB for networks.
- Keep Discord webhook URLs private.
- Give staff granular permissions instead of only `sentinelreports.admin`.
- Set sensible cooldowns for public servers.
- Keep exports outside publicly served web roots unless intentional.
- Back up the database before major plugin upgrades.

## Troubleshooting

- `Player not found`: direct report commands require the target to be online.
- `Invalid category`: verify `categories.<id>.enabled: true`.
- Discord test does nothing: verify `discord.enabled: true` and `webhook_url`.
- SQLite locked: use MySQL/MariaDB for multi-server setups.
