package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import javax.sql.*;
import javax.naming.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class MitarbeiterRückgabeBenachrichtigungServlet_Original extends HttpServlet {


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("management_standort_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int standortId = (Integer) session.getAttribute("management_standort_id");

          List<JsonObject> rueckgabeAnfragen = new ArrayList<>();

        try {

            Context initCtx = new InitialContext();

            DataSource ds =(DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
            try(Connection connection = ds.getConnection()){

                String query = """
                    SELECT m.Mitglied_id AS mitglied_id, m.vorname AS vorname, m.nachname AS nachname,
                           m.email AS email, m.addresse AS addresse, m.stat AS mitglied_status,
                           r.id AS reservierung_id, r.reserviert_am AS ausgeliehen_am, r.zurueckgeben AS zurueckgeben,
                           r.gebuehr AS gebuehr, r.fehl_tage AS fehl_tage, r.status AS status,
                           e.id AS exemplar_id, e.barcode AS barcode, e.regal AS regal, e.zustand AS zustand,
                           b.titel AS titel, b.autor AS autor, b.genre AS genre, s.name AS standort
                    FROM reservierung r
                    JOIN mitglied m ON r.mitglied_id = m.Mitglied_id
                    JOIN exemplar e ON r.exemplar_id = e.id
                    JOIN buch b ON e.buch_id = b.id
                    JOIN standort s ON e.standort_id = s.id
                    WHERE r.status = 'RUECKGABE_ANGEFRAGT'
                      AND e.verfuegbar = 0
                      AND e.zustand IN ('NEU', 'GUT')
                      AND e.standort_id = ?
                    ORDER BY m.Mitglied_id, r.reserviert_am
                    """;

            try(PreparedStatement ps = connection.prepareStatement(query)){
            ps.setInt(1, standortId);

            try(ResultSet rs = ps.executeQuery()){
           
              while (rs.next()) {

                            String mitgliedName = rs.getString("vorname")+ " "+ rs.getString("nachname");

                            JsonObject exemplarJson = new JsonObject();

                            exemplarJson.addProperty("mitgliedId", rs.getInt("mitglied_id"));
                            exemplarJson.addProperty("mitgliedName", mitgliedName);
                            exemplarJson.addProperty("email", rs.getString("email"));
                            exemplarJson.addProperty("addresse", rs.getString("addresse"));
                            exemplarJson.addProperty("mitgliedStatus", rs.getString("mitglied_status"));
                            exemplarJson.addProperty("reservierungId", rs.getInt("reservierung_id"));
                            exemplarJson.addProperty("exemplarId", rs.getInt("exemplar_id"));
                            exemplarJson.addProperty("titel", rs.getString("titel"));
                            exemplarJson.addProperty("autor", rs.getString("autor"));
                            exemplarJson.addProperty("genre", rs.getString("genre"));
                            exemplarJson.addProperty("barcode", rs.getString("barcode"));
                            exemplarJson.addProperty("standort", rs.getString("standort"));
                            exemplarJson.addProperty("regal", rs.getString("regal"));
                            exemplarJson.addProperty("zustand", rs.getString("zustand"));
                            exemplarJson.addProperty("gebuehr", rs.getBigDecimal("gebuehr"));
                            exemplarJson.addProperty("fehlTage", rs.getInt("fehl_tage"));
                            exemplarJson.addProperty("status", rs.getString("status"));
                            Date ausgeliehenAm = rs.getDate("ausgeliehen_am");
                            Date zurueckgeben =rs.getDate("zurueckgeben");

                            exemplarJson.addProperty("ausgeliehenAm", ausgeliehenAm != null ? ausgeliehenAm.toString() : null);
                            exemplarJson.addProperty("zurueckgeben", zurueckgeben != null ? zurueckgeben.toString() : null);
                            
                            rueckgabeAnfragen.add(exemplarJson);
                        }
                      }
                     }
                    }
                      Gson gson = new Gson();
                      String rueckgabeJson = gson.toJson(rueckgabeAnfragen);
                      response.getWriter().println(rueckgabeJson);
       
              } catch (SQLException | NamingException e) {
                 e.printStackTrace();
                 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
              }
    
           }






    @Override
 protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    response.setContentType("text/plain;charset=UTF-8");

    PrintWriter out = response.getWriter();

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("management_standort_id") == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

     String mitgliedIdText = request.getParameter("mitgliedid");
     int mitgliedId = Integer.parseInt(mitgliedIdText);

  
    int standortId =(Integer) session.getAttribute("management_standort_id");

       Connection connection = null;

        try {

        Context initCtx = new InitialContext();

        DataSource ds =(DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

        connection = ds.getConnection();

        connection.setAutoCommit(false);




















        /*
         * Prüfen, ob bei den aktuell zu bestätigenden
         * Rückgabeanfragen mindestens ein Exemplar
         * 10 oder mehr Fehltage hat.
         */
        String ueberfaelligPruefenQuery = """
            SELECT COUNT(*) AS anzahl
            FROM reservierung r
            JOIN exemplar e ON r.exemplar_id = e.id
            WHERE r.mitglied_id = ?
              AND r.status = 'RUECKGABE_ANGEFRAGT'
              AND r.fehl_tage >= 10
              AND e.standort_id = ?
              AND e.verfuegbar = 0
              AND e.zustand IN ('NEU', 'GUT')
            """;

        int ueberfaelligeAnfragen = 0;

        try (PreparedStatement ps = connection.prepareStatement(ueberfaelligPruefenQuery)) {

            ps.setInt(1, mitgliedId);
            ps.setInt(2, standortId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    ueberfaelligeAnfragen = rs.getInt("anzahl");
                }
            }
        }


        /*
         * Alle Rückgabeanfragen dieses Mitglieds
         * am Standort bestätigen:
         *
         * Status -> ZURUECKGEGEBEN
         * Rückgabedatum -> heute
         * Exemplar -> verfügbar
         *
         * Gebühr und Fehltage bleiben unverändert.
         */
        String rueckgabeQuery = """
            UPDATE reservierung r
            JOIN exemplar e ON r.exemplar_id = e.id
            SET r.status = 'ZURUECKGEGEBEN',
                r.zurueckgeben = CURDATE(),
                e.verfuegbar = 1
            WHERE r.mitglied_id = ?
              AND r.status = 'RUECKGABE_ANGEFRAGT'
              AND e.standort_id = ?
              AND e.verfuegbar = 0
              AND e.zustand IN ('NEU', 'GUT')
            """;
           
        int anzahlZurueckgegeben; 
       
        try (PreparedStatement ps = connection.prepareStatement(rueckgabeQuery)) {

            ps.setInt(1, mitgliedId);
            ps.setInt(2, standortId);

           anzahlZurueckgegeben = ps.executeUpdate();
        }


        if (anzahlZurueckgegeben == 0) {
           
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            out.println("Keine offenen Rückgabeanfragen gefunden.");
            return;
        }


        boolean mitgliedAktiviert = false;
        boolean weitereUeberfaelligeExemplare = false;


        /*
         * Mitglied nur dann überprüfen und eventuell
         * entsperren, wenn mindestens eine der gerade
         * zurückgegebenen Anfragen überfällig war.
         */
        if (ueberfaelligeAnfragen > 0) {

            /* 
             * Standort wird hier absichtlich NICHT geprüft.
             *
             * Ein überfälliges Exemplar an einem anderen
             * Standort muss das Mitglied weiterhin sperren.
             */
            String offeneUeberfaelligeQuery = """
                SELECT COUNT(*) AS anzahl
                FROM reservierung r
                JOIN exemplar e ON r.exemplar_id = e.id
                WHERE r.mitglied_id = ?
                  AND r.status IN ('AUSGELIEHEN','RUECKGABE_ANGEFRAGT')
                  AND r.fehl_tage >= 10
                  AND e.verfuegbar = 0
                """;

            int offeneUeberfaellige = 0;

            try (PreparedStatement ps =connection.prepareStatement(offeneUeberfaelligeQuery)) {

                ps.setInt(1, mitgliedId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        offeneUeberfaellige = rs.getInt("anzahl");
                    }
                }
            }


            /*
             * Keine andere überfällige Ausleihe vorhanden:
             * Mitglied wieder aktivieren.
             */
            if (offeneUeberfaellige == 0) {

                String aktivierenQuery = """
                    UPDATE mitglied
                    SET stat = 'AKTIV'
                    WHERE Mitglied_id = ?
                      AND stat = 'GESPERRT'
                    """;

                try (PreparedStatement ps = connection.prepareStatement(aktivierenQuery)) {

                    ps.setInt(1, mitgliedId);

                    int aktiviert = ps.executeUpdate();

                    mitgliedAktiviert = aktiviert > 0;
                }

            } else {

                weitereUeberfaelligeExemplare = true;
            }
        }


        connection.commit();

        response.setStatus(HttpServletResponse.SC_OK);


        if (mitgliedAktiviert) {

            out.println("Die angefragten Exemplar(e) wurden zurückgegeben. Das Mitglied wurde wieder aktiviert.");

        } else if (weitereUeberfaelligeExemplare) {

            out.println("Die angefragte Exemplar(e) wurden zurückgegeben. Das Mitglied bleibt gesperrt, weil noch mindestens ein anderes überfälliges Exemplar vorhanden ist.");

        } else {

            out.println("Die angefragte Exemplar(e) wurden erfolgreich zurückgegeben.");
        }


    } catch (SQLException | NamingException e) {

        if (connection != null) {

            try {
                connection.rollback();
            } catch (SQLException rollbackFehler) {
                rollbackFehler.printStackTrace();
            }
        }

        e.printStackTrace();

        response.setStatus(
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        );

     
    } finally {

        if (connection != null) {

            try {

                connection.setAutoCommit(true);
                connection.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
  }
}











