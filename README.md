Webbasierte Bibliotheksanwendung

Eine vollständige Java-Webanwendung zur digitalen Verwaltung zentraler Bibliotheksabläufe. Die Anwendung wurde mit Java, Jakarta Servlets, HTML, CSS, JavaScript und MariaDB entwickelt und 
unterstützt unterschiedliche Benutzerrollen mit eigenen Berechtigungen.

Projektziel

Ziel des Projekts ist es, typische Abläufe einer Bibliothek digital abzubilden. Dazu gehören unter anderem die Verwaltung von Büchern, Exemplaren, Mitgliedern, Mitarbeitern und Standorten
sowie die Durchführung von Reservierungen, Ausleihen und Rückgaben.

Die Anwendung besitzt ein rollenbasiertes Berechtigungssystem für:

Manager, Mitarbeiter und Mitglieder

Jede Rolle erhält nur Zugriff auf die für sie vorgesehenen Funktionen.

Benutzerrollen und Funktionen
Manager

Manager können:

sich einloggen und ausloggen,
Bücher hinzufügen und löschen,
Exemplare hinzufügen und löschen,
Standorte hinzufügen,
Mitarbeiter hinzufügen, ansehen und löschen,
Mitglieder ansehen,
die Ausleihhistorie von Mitgliedern ansehen,
alle Exemplare ansehen,
Inventuren durchführen,
Statistiken ansehen


Mitarbeiter können:

sich einloggen und ausloggen,
Exemplare hinzufügen und löschen,
Mitglieder ansehen,
die Ausleihhistorie von Mitgliedern ansehen,
alle Exemplare ansehen,
Benachrichtigungen einsehen,
Ausleihen am eigenen Standort durchführen,
Rückgaben am eigenen Standort durchführen,
Mitglieder History Ansehen

Mitglieder können:

sich registrieren und Bestätigung per E-mail bekommen,
sich einloggen und ausloggen,
Bücher ansehen,
Exemplare reservieren,
Exemplare in einem Warenkorb speichern,
aktuelle Ausleihen in Webseite ansehen und per E-mail bekommen,
die eigene Ausleihhistorie ansehen,
Rückgabeanfragen stellen,
Quittungen als PDF mit QR-Code herunterladen

Verwendete Technologien
Backend

Java,
Jakarta Servlets,
Apache Tomcat,
JDBC,
JNDI,
Gson,
MariaDB,
Redis,
HttpSession,
PBKDF2WithHmacSHA512,
SecureRandom


Frontend


HTML,
CSS,
JavaScript,
AJAX,
JSON


Deployment und Infrastruktur


Docker,
Apache Tomcat,
HAProxy,
WAR-Deployment,
Shell-Skripte,
Tomcat Manager










