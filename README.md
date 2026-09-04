# Webbasierte Bibliotheksanwendung

Eine Java-Webanwendung zur digitalen Verwaltung zentraler Bibliotheksabläufe.

Die Anwendung wurde mit Java, Jakarta Servlets, HTML, CSS, JavaScript und MariaDB entwickelt. Sie unterstützt mehrere Benutzerrollen mit unterschiedlichen Berechtigungen und bildet typische Prozesse einer Bibliothek digital ab.

## Projektziel

Ziel des Projekts ist es, zentrale Abläufe einer Bibliothek digital abzubilden.

Dazu gehören unter anderem:

* Verwaltung von Büchern
* Verwaltung von Exemplaren
* Verwaltung von Mitgliedern
* Verwaltung von Mitarbeitern
* Verwaltung von Bibliotheksstandorten
* Reservierung von Exemplaren
* Ausleihe von Exemplaren
* Rückgabe von Exemplaren
* Ausleihhistorie
* PDF-Ausleihbelege
* QR-Codes
* E-Mail-Benachrichtigungen
* Inventur
* Statistiken

Die Anwendung verwendet ein rollenbasiertes Berechtigungssystem für:

* Manager
* Mitarbeiter
* Mitglieder

Jede Rolle erhält nur Zugriff auf die für sie vorgesehenen Funktionen.

# Benutzerrollen und Funktionen

## Manager

Manager können:

* sich einloggen und ausloggen
* Bücher hinzufügen
* Bücher löschen
* Exemplare hinzufügen
* Exemplare löschen
* Standorte hinzufügen
* Mitarbeiter hinzufügen
* Mitarbeiter ansehen
* Mitarbeiter löschen
* Mitglieder ansehen
* die Ausleihhistorie von Mitgliedern ansehen
* alle Exemplare ansehen
* Inventuren durchführen
* Statistiken ansehen

## Mitarbeiter

Mitarbeiter können:

* sich einloggen und ausloggen
* Exemplare hinzufügen
* Exemplare löschen
* Mitglieder ansehen
* die Ausleihhistorie von Mitgliedern ansehen
* alle Exemplare ansehen
* Benachrichtigungen einsehen
* Ausleihen am eigenen Standort bestätigen
* Rückgaben am eigenen Standort bestätigen
* die Historie von Mitgliedern ansehen

## Mitglieder

Mitglieder können:

* sich registrieren
* nach erfolgreicher Registrierung eine Bestätigungs-E-Mail erhalten
* sich einloggen und ausloggen
* Bücher ansehen
* verfügbare Exemplare reservieren
* Exemplare in einem Warenkorb speichern
* aktuelle Ausleihen auf der Webseite ansehen
* nach einer bestätigten Ausleihe eine E-Mail erhalten
* einen Ausleihbeleg als PDF per E-Mail erhalten
* einen QR-Code zur Ausleihe erhalten
* die eigene Ausleihhistorie ansehen
* Rückgabeanfragen stellen
* Quittungen beziehungsweise Ausleihbelege als PDF herunterladen

# E-Mail-Funktion

Die Anwendung unterstützt echten E-Mail-Versand.

Nach erfolgreicher Registrierung erhält das Mitglied eine Bestätigungs-E-Mail.

Nach der Bestätigung einer Ausleihe durch einen Mitarbeiter erhält das Mitglied eine E-Mail mit:

* Informationen zur bestätigten Ausleihe
* PDF-Ausleihbeleg
* QR-Code


# Verwendete Technologien

## Backend

* Java
* Jakarta Servlets
* Apache Tomcat
* JDBC
* JNDI
* MariaDB
* Redis
* Gson
* HttpSession
* PBKDF2WithHmacSHA512
* SecureRandom
* JavaMail / SMTP
* iText
* ZXing

## Frontend

* HTML
* CSS
* JavaScript
* AJAX
* JSON

## Deployment und Infrastruktur

* Docker
* Apache Tomcat
* HAProxy
* WAR-Deployment
* Shell-Skripte
* Tomcat Manager

# Voraussetzungen

Zum Ausführen der Anwendung werden unter anderem benötigt:

* Java JDK
* Apache Tomcat
* MariaDB
* Redis
* Docker beziehungsweise die entsprechende Serverumgebung
* Git

Die benötigten Java-Bibliotheken befinden sich im Projekt beziehungsweise müssen für den Build verfügbar sein.

# Projektstruktur

Eine vereinfachte Struktur des Projekts:

```text
Bibliothekswebanwendung/
├── app/
│   ├── WEB-INF/
│   │   ├── web.xml
│   │   └── lib/
│   ├── HTML-Dateien
│   ├── CSS-Dateien
│   └── JavaScript-Dateien
│
├── src/
│   └── hbv/
│       └── web/
│           ├── Servlets
│           ├── MailService
│           ├── PDF
│           └── QRCode
│
├── bin/
│   ├── build.sh
│   └── deploy.sh
│
├── lib/
│   └── Java-Bibliotheken
│
├── local/
│   └── lokale Konfiguration
│
└── README.md
```

# Lokale Konfiguration

Die lokale Konfiguration kann beispielsweise folgende Werte enthalten:

```text
dbserver=...
dbuser=...
dbpassword=...
dbname=...

redisserver=...
redispassword=...

mailuser=...
mailpassword=...
```


# Sicherheit

Die Anwendung verwendet unter anderem:

* rollenbasierte Zugriffssteuerung
* Sessions für angemeldete Benutzer
* PBKDF2WithHmacSHA512 zur Passwortverarbeitung
* SecureRandom
* PreparedStatements für Datenbankzugriffe

# Build

Die Anwendung kann über das vorhandene Build-Skript gebaut werden:

```bash
bin/build.sh
```

Das Skript:

1. liest die lokale Konfiguration
2. erstellt das Build-Verzeichnis
3. kompiliert die Java-Klassen
4. erstellt eine WAR-Datei
5. deployt die Anwendung über den Tomcat Manager


# Deployment

Die Anwendung wird als WAR-Datei auf Apache Tomcat deployed.

Das Deployment erfolgt über die vorhandenen Shell-Skripte und den Tomcat Manager.

Die produktive Umgebung verwendet zusätzlich Docker und HAProxy.


# PDF und QR-Code

Bei einer bestätigten Ausleihe kann ein PDF-Ausleihbeleg erzeugt werden.

Der Beleg enthält Informationen zum Mitglied sowie zu den ausgeliehenen Exemplaren.

Zusätzlich wird ein QR-Code erzeugt, der für die Anzeige beziehungsweise Verarbeitung der Ausleihinformationen verwendet werden kann.

