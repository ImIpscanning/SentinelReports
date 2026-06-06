<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:111827,50:374151,100:E5E7EB&height=210&section=header&text=SentinelReports&fontSize=56&fontColor=FFFFFF&animation=fadeIn&fontAlignY=38&desc=Production-ready%20reports%20system%20for%20Minecraft%20networks&descAlignY=58&descSize=15" alt="SentinelReports Header" />

<img src="https://readme-typing-svg.demolab.com?font=JetBrains+Mono&size=19&duration=2600&pause=900&color=E5E7EB&center=true&vCenter=true&width=820&height=40&lines=Manage+player+reports+professionally.;Track+evidence,+notes+and+staff+workflow.;Built+for+Velocity,+Paper+and+Purpur.;Report.+Review.+Resolve." alt="Typing Animation" />

<p>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Platform-Velocity%20%7C%20Paper%20%7C%20Purpur-374151?style=for-the-badge" alt="Platforms" />
  <img src="https://img.shields.io/badge/Database-SQLite%20%7C%20MySQL%20%7C%20MariaDB-4B5563?style=for-the-badge" alt="Database" />
  <img src="https://img.shields.io/badge/Focus-Report%20Management-111827?style=for-the-badge" alt="Report Management" />
</p>

</div>

---

## Overview

**SentinelReports** is a production-oriented report management system for Minecraft networks using **Velocity** as proxy and **Paper/Purpur** backend servers.

It provides a complete workflow for player reports, staff review, evidence tracking, internal notes, report history, Discord alerts, GUI menus, exports and anti-abuse protections.

```diff
+ Player-to-player reports
+ Staff claim, assign and review workflow
+ Evidence and internal notes
+ Report history by reporter and target
+ Paper GUI for players and staff
+ Velocity global commands and staff alerts
+ SQLite for local testing
+ MySQL/MariaDB for production networks
+ Discord webhook alerts
+ JSON and HTML exports
+ PlaceholderAPI support
+ Public API for integrations

- Not an anticheat
- Not an anti-exploit plugin
- Not a login/auth system
- Not a full punishment core
```

---

## Modules

```txt
sentinelreports-common
Shared models, storage, migrations, services, API, command logic,
messages, report workflow and anti-abuse utilities.

sentinelreports-paper
Paper/Purpur plugin with player commands, staff commands, GUI menus,
PlaceholderAPI support and backend report interactions.

sentinelreports-velocity
Velocity proxy plugin with global commands, network-wide report access
and staff alerts.
```

---

## Features

```txt
Configurable report categories
Custom report reasons
Report priorities
Cooldown protection
Duplicate report prevention
Evidence system
Internal staff notes
Claim / unclaim workflow
Assign workflow
Report status management
Reporter and target history
Discord webhook notifications
JSON and HTML report exports
Paper GUI menus
Velocity global commands
PlaceholderAPI expansion
Public API through SentinelReportsProvider
```

---

## Report States

```txt
OPEN
ASSIGNED
REVIEWING
WAITING_EVIDENCE
RESOLVED
REJECTED
FALSE_REPORT
CLOSED
```

---

## Build

### Requirements

```txt
Java 21
Gradle Wrapper included
```

### Build command

```bash
./gradlew clean test shadowJar
```

### Generated artifacts

```txt
sentinelreports-paper/build/libs/SentinelReports-Paper-1.0.0.jar
sentinelreports-velocity/build/libs/SentinelReports-Velocity-1.0.0.jar
```

---

## Installation

### Paper / Purpur

```txt
1. Build or download SentinelReports-Paper-1.0.0.jar.
2. Place it inside the backend server plugins/ folder.
3. Start the server once.
4. Edit config.yml, messages.yml and gui.yml.
5. Restart the server or run /reports reload.
```

```txt
paper-server/
└── plugins/
    └── SentinelReports-Paper-1.0.0.jar
```

### Velocity

```txt
1. Build or download SentinelReports-Velocity-1.0.0.jar.
2. Place it inside the Velocity plugins/ folder.
3. Start the proxy once.
4. Edit plugins/sentinelreports/config.yml.
5. Use the same MySQL/MariaDB database as Paper servers for network-wide state.
```

```txt
velocity/
└── plugins/
    └── SentinelReports-Velocity-1.0.0.jar
```

---

## Database

SQLite is the default option for local testing.

```yaml
database:
  type: sqlite
  sqlite_file: "sentinelreports.db"
```

For production networks, MySQL or MariaDB is recommended.

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

Migrations are versioned under:

```txt
sentinelreports-common/src/main/resources/db/migration
```

---

## Discord Webhooks

SentinelReports can send Discord alerts for important report activity.

```yaml
discord:
  enabled: true
  webhook_url: "https://discord.com/api/webhooks/..."
  notify_new_report: true
  notify_critical_report: true
  notify_report_closed: true
```

Webhook URLs are never shown in chat or command output.

---

## Player Commands

```txt
/report
/report help
/report <player>
/report <player> <category> <reason>
/report cancel
/report status
/report history
```

---

## Staff Commands

```txt
/reports
/reports gui
/reports hub
/reports help [page|player|staff|evidence|notes|history|admin]
/reports list [open|assigned|closed|status]
/reports view <id>
/reports claim <id>
/reports unclaim <id>
/reports assign <id> <staff>
/reports status <id> <status>
/reports priority <id> <priority>
/reports close <id> <reason>
/reports reject <id> <reason>
/reports false <id> <reason>
/reports evidence add <id> <text/link>
/reports evidence list <id>
/reports evidence remove <id> <evidenceId>
/reports note add <id> <note>
/reports note list <id>
/reports history <player>
/reports against <player>
/reports by <player>
/reports teleport <id>
/reports server <id>
/reports notify <id>
/reports stats [staff]
/reports reload
/reports discord test
/reports export <id> <json|html>
```

---

## Permissions

```txt
sentinelreports.admin
sentinelreports.report
sentinelreports.report.staff
sentinelreports.staff
sentinelreports.staff.list
sentinelreports.staff.view
sentinelreports.staff.claim
sentinelreports.staff.assign
sentinelreports.staff.close
sentinelreports.staff.reject
sentinelreports.staff.false
sentinelreports.staff.note
sentinelreports.staff.evidence
sentinelreports.staff.history
sentinelreports.staff.teleport
sentinelreports.staff.stats
sentinelreports.staff.reload
sentinelreports.staff.discord
sentinelreports.staff.export
sentinelreports.bypass.cooldown
sentinelreports.bypass.limit
```

---

## GUI

Paper/Purpur includes configurable GUI menus for players and staff.

```txt
Player main report menu
Category selection menu
Staff hub menu
Open reports menu
Report detail menu
Claim, evidence, notes, teleport, critical and resolve actions
```

All menu titles, items, slots, materials and lore are configurable in:

```txt
gui.yml
```

---

## Anti-Abuse

SentinelReports includes configurable protection against report spam and abuse.

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

The system can block:

```txt
Self-reports
Duplicate recent reports
Cooldown abuse
Excessive open reports
Repeated false report behavior
```

Bypass permissions are available for trusted staff or specific groups.

---

## PlaceholderAPI

Paper placeholders:

```txt
%sentinelreports_open_reports%
%sentinelreports_assigned_reports%
%sentinelreports_player_reports%
%sentinelreports_player_false_reports%
```

---

## Public API

Other plugins can interact with SentinelReports through the public API.

```java
SentinelReportsAPI api = SentinelReportsProvider.get();

api.createReport(request);
api.closeReport(15L, ReportStatus.RESOLVED, staffUuid, "Moderator", "Handled");
api.addEvidence(15L, "https://example.com/video", staffUuid, "Moderator");
api.addNote(15L, "Checked logs", staffUuid, "Moderator");
api.getOpenReports();
```

Use this if your plugin should stay safe when SentinelReports is not installed:

```java
SentinelReportsProvider.getOptional();
```

---

## Export System

Reports can be exported for evidence, review or staff records.

```txt
/reports export <id> json
/reports export <id> html
```

Supported formats:

```txt
JSON
HTML
```

---

## Staff Workflow

```txt
Player creates report
        ↓
Staff receives alert
        ↓
Report is reviewed
        ↓
Staff claims or assigns the report
        ↓
Evidence and notes are added
        ↓
Report is resolved, rejected or marked false
        ↓
Report can be exported if needed
```

---

## Production Recommendations

```txt
Use MySQL/MariaDB for network setups.
Keep Discord webhook URLs private.
Use granular staff permissions instead of only sentinelreports.admin.
Set sensible cooldowns for public servers.
Keep exports outside public web roots unless intentional.
Back up the database before major plugin upgrades.
```

---

## Troubleshooting

```txt
Player not found
Direct report commands require the target to be online.

Invalid category
Verify categories.<id>.enabled: true.

Discord test does nothing
Verify discord.enabled: true and webhook_url.

SQLite locked
Use MySQL or MariaDB for multi-server setups.
```

---

## Project Structure

```txt
SentinelReports/
├── .github/
│   └── workflows/
├── gradle/
│   └── wrapper/
├── sentinelreports-common/
├── sentinelreports-paper/
├── sentinelreports-velocity/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

---

## Technologies

<p align="center">
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/java/java-original.svg" width="46" height="46" alt="Java" />
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/gradle/gradle-original.svg" width="46" height="46" alt="Gradle" />
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/mysql/mysql-original.svg" width="46" height="46" alt="MySQL" />
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/sqlite/sqlite-original.svg" width="46" height="46" alt="SQLite" />
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/git/git-original.svg" width="46" height="46" alt="Git" />
  <img src="https://raw.githubusercontent.com/devicons/devicon/master/icons/github/github-original.svg" width="46" height="46" alt="GitHub" />
</p>

---

## Philosophy

```txt
Report.
Review.
Document.
Resolve.
```

SentinelReports is designed to make moderation workflows cleaner, more organized and easier to audit across Minecraft networks.

---

## Status

```diff
+ Velocity and Paper/Purpur support
+ Player report system
+ Staff workflow
+ Evidence and notes
+ Discord webhook alerts
+ Paper GUI
+ Anti-abuse protection
+ JSON and HTML exports
+ PlaceholderAPI expansion
+ Public API
```

---

## Author

Developed by **ipscanning**.

```txt
Better reports.
Cleaner moderation.
Stronger staff workflow.
```

<div align="center">

<img src="https://readme-typing-svg.demolab.com?font=JetBrains+Mono&size=18&duration=2600&pause=1000&color=E5E7EB&center=true&vCenter=true&width=720&height=40&lines=Thanks+for+checking+out+SentinelReports.;Built+for+Minecraft+network+moderation.;Report.+Review.+Resolve." alt="Footer Typing" />

<strong>SentinelReports</strong> — Minecraft Network Reports System

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:E5E7EB,50:374151,100:111827&height=110&section=footer" alt="Footer" />

</div>
