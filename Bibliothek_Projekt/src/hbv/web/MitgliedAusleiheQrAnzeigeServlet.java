package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;

import javax.sql.*;
import javax.naming.*;

public class MitgliedAusleiheQrAnzeigeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html;charset=UTF-8");
        String mitgliedIdText = request.getParameter("mitgliedid");

        String ausgeliehenAm = request.getParameter("ausgeliehenAm");

        String zurueckgeben = request.getParameter("zurueckgeben");

        if(mitgliedIdText == null || ausgeliehenAm == null || zurueckgeben == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Fehlende Daten im QR-Code.");
            return;
        }

        int mitgliedId = Integer.parseInt(mitgliedIdText);

        try {

            Context initCtx = new InitialContext();

            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try(Connection connection = ds.getConnection()) {

                String query =
                    "SELECT "
                    + "m.Mitglied_id AS mitglied_id, "
                    + "m.vorname AS vorname, "
                    + "m.nachname AS nachname, "
                    + "m.email AS email, "
                    + "m.addresse AS addresse, "
                    + "e.id AS exemplar_id, "
                    + "e.barcode AS barcode, "
                    + "e.regal AS regal, "
                    + "e.zustand AS zustand, "
                    + "b.titel AS titel, "
                    + "b.autor AS autor, "
                    + "b.genre AS genre, "
                    + "s.name AS standort, "
                    + "r.reserviert_am AS ausgeliehen_am, "
                    + "r.zurueckgeben AS zurueckgeben, "
                    + "r.gebuehr AS gebuehr, "
                    + "r.fehl_tage AS fehl_tage "
                    + "FROM reservierung r "
                    + "JOIN mitglied m ON r.mitglied_id = m.Mitglied_id "
                    + "JOIN exemplar e ON r.exemplar_id = e.id "
                    + "JOIN buch b ON e.buch_id = b.id "
                    + "JOIN standort s ON e.standort_id = s.id "
                    + "WHERE r.mitglied_id = ? "
                    + "AND r.reserviert_am = ? "
                    + "AND r.zurueckgeben = ? "
                    + "AND r.status IN ('AUSGELIEHEN', 'RUECKGABE_ANGEFRAGT') "
                    + "ORDER BY e.id";

                PreparedStatement ps = connection.prepareStatement(query);

                ps.setInt(1, mitgliedId);
                ps.setDate(2, java.sql.Date.valueOf(ausgeliehenAm));
                ps.setDate(3, java.sql.Date.valueOf(zurueckgeben));

                ResultSet rs =ps.executeQuery();

                PrintWriter out = response.getWriter();

                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<title>Ausleihbeleg</title>");

                out.println("<style>");
                out.println("body { font-family: Arial; background:white; padding:30px; }");
                out.println("h1 { text-align:center; }");
                out.println("table { width:100%; border-collapse:collapse; margin-top:20px; }");
                out.println("th, td { border:1px solid #999; padding:8px; font-size:14px; }");
                out.println("th { background:#eeeeee; }");
                out.println("</style>");

                out.println("</head>");
                out.println("<body>");

                out.println("<h1>Ausleihbeleg / Bibliothek</h1>");

                boolean first =true;
                boolean gefunden =false;

                while(rs.next()) {
                    gefunden = true;
                    if(first) {

                        out.println("<h2>Mitgliedsdaten</h2>");
                        out.println("<p><b>Mitglied-ID:</b> "
                            + rs.getInt("mitglied_id") + "</p>");
                        out.println("<p><b>Name:</b> "
                            + rs.getString("vorname") + " "
                            + rs.getString("nachname")+ "</p>");
                        out.println("<p><b>E-Mail:</b> "
                            + rs.getString("email")+ "</p>");
                        out.println("<p><b>Adresse:</b> "
                            + rs.getString("addresse")+ "</p>");

                        out.println("<br><hr><br><h2>Ausleihdaten</h2>");
                        out.println("<p><b>Ausgeliehen am:</b> "
                            + rs.getDate("ausgeliehen_am") + "</p>");
                        out.println("<p><b>Rückgabedatum:</b> "
                            + rs.getDate("zurueckgeben")+ "</p>");

                        out.println("<br><hr><br><h2>Ausgeliehene Exemplare</h2><br>");

                        out.println("<table>");
                        out.println("<tr>");
                        out.println("<th>Exemplar-ID</th>");
                        out.println("<th>Titel</th>");
                        out.println("<th>Autor</th>");
                        out.println("<th>Barcode</th>");
                        out.println("<th>Standort</th>");
                        out.println("<th>Genre</th>");
                        out.println("<th>Regal</th>");
                        out.println("<th>Zustand</th>");
                        out.println("<th>Gebühr</th>");
                        out.println("<th>Fehltage</th>");
                        out.println("</tr>");

                        first = false;
                    }

                    out.println("<tr>");
                    out.println("<td>" + rs.getInt("exemplar_id") + "</td>");
                    out.println("<td>" + rs.getString("titel") + "</td>");
                    out.println("<td>" + rs.getString("autor") + "</td>");
                    out.println("<td>" + rs.getString("barcode") + "</td>");
                    out.println("<td>" + rs.getString("standort") + "</td>");
                    out.println("<td>" + rs.getString("genre") + "</td>");
                    out.println("<td>" + rs.getString("regal") + "</td>");
                    out.println("<td>" + rs.getString("zustand") + "</td>");
                    out.println("<td>" + rs.getBigDecimal("gebuehr") + " €</td>");

                    int fehlTage = rs.getInt("fehl_tage");

                    if(fehlTage >= 14) {
                        out.println("<td style='color:red;font-weight:bold'>"+ fehlTage + " Überfällig</td>");
                    } else {
                        out.println("<td>" + fehlTage + "</td>");
                    }
                        out.println("</tr>");
                }

                if(gefunden) {
                    out.println("</table>");
                } else {
                    out.println("<p>Keine Ausleihdaten gefunden.</p>");
                }

                out.println("</body>");
                out.println("</html>");

                rs.close();
                ps.close();
            }

        } catch(Exception e) {

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Fehler beim Anzeigen der QR-Daten: "+ e.getMessage());
        }
    }
}
