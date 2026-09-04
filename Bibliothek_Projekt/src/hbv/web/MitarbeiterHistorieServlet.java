package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class MitarbeiterHistorieServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            HttpSession session = request.getSession(false);

            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            

             //Aus Session Holen
            String rolle = (String) session.getAttribute("rolle");
            Integer standortId = (Integer) session.getAttribute("management_standort_id");



            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try (Connection connection = ds.getConnection()) {
                BibliothekAutomatikService.aktualisiereGebuehrFehltageUndSperre(connection);

                String query;
                PreparedStatement ps;


                // Manager sieht alle, Mitarbeiter nur die seines Standorts
                if ("Manager".equals(rolle)) {
                    query =
                        "SELECT r.id AS reservierung_id, r.exemplar_id, r.mitglied_id, " +
                        "r.reserviert_am, r.zurueckgeben, r.status, r.gebuehr, r.fehl_tage, " +
                        "b.titel, b.autor, b.genre, " +
                        "e.barcode, e.regal, e.zustand, " +
                        "s.name AS standort, " +
                        "m.vorname, m.nachname, m.email, m.addresse, m.stat " +
                        "FROM reservierung r " +
                        "JOIN exemplar e ON r.exemplar_id = e.id " +
                        "JOIN buch b ON e.buch_id = b.id " +
                        "LEFT JOIN standort s ON e.standort_id = s.id " +
                        "JOIN mitglied m ON r.mitglied_id = m.Mitglied_id " +
                        "ORDER BY r.mitglied_id, r.reserviert_am DESC";
                    ps = connection.prepareStatement(query);
                
                        
                } else {

                    // Mitarbeiter sieht nur Reservierungen von Mitgliedern seines Standorts
                    query =
                        "SELECT r.id AS reservierung_id, r.exemplar_id, r.mitglied_id, " +
                        "r.reserviert_am, r.zurueckgeben, r.status, r.gebuehr, r.fehl_tage, " +
                        "b.titel, b.autor, b.genre, " +
                        "e.barcode, e.regal, e.zustand, " +
                        "s.name AS standort, " +
                        "m.vorname, m.nachname, m.email, m.addresse , m.stat " +
                        "FROM reservierung r " +
                        "JOIN exemplar e ON r.exemplar_id = e.id " +
                        "JOIN buch b ON e.buch_id = b.id " +
                        "LEFT JOIN standort s ON e.standort_id = s.id " +
                        "JOIN mitglied m ON r.mitglied_id = m.Mitglied_id " +
                        "WHERE e.standort_id = ? " +
                        "ORDER BY r.mitglied_id, r.reserviert_am DESC";
                    ps = connection.prepareStatement(query);
                    ps.setInt(1, standortId);
                }



                ResultSet rs = ps.executeQuery();

                out.print("[");
                boolean first = true;

                while (rs.next()) {
                    if (!first) {
                        out.print(",");
                    }

                  String name = rs.getString("vorname") + " " + rs.getString("nachname");
                  
                  out.println("{");
                  out.println("\"reservierungId\":" + rs.getInt("reservierung_id") + ",");
                  out.println("\"exemplarId\":" + rs.getInt("exemplar_id") + ",");
                  out.println("\"mitgliedId\":" + rs.getInt("mitglied_id") + ",");
                  out.println("\"name\":\"" + name + "\",");
                  out.println("\"mitgliedStatus\":\"" + rs.getString("stat") + "\",");
                  out.println("\"email\":\"" + rs.getString("email") + "\",");
                  out.println("\"addresse\":\"" + rs.getString("addresse") + "\",");
                  out.println("\"titel\":\"" + rs.getString("titel") + "\",");
                  out.println("\"autor\":\"" + rs.getString("autor") + "\",");
                  out.println("\"genre\":\"" + rs.getString("genre") + "\",");
                  out.println("\"barcode\":\"" + rs.getString("barcode") + "\",");
                  out.println("\"standort\":\"" + rs.getString("standort") + "\",");
                  out.println("\"regal\":\"" + rs.getString("regal") + "\",");
                  out.println("\"zustand\":\"" + rs.getString("zustand") + "\",");
                  out.println("\"ausgeliehenAm\":\"" + rs.getString("reserviert_am") + "\",");
                  out.println("\"zurueckgeben\":\"" + rs.getString("zurueckgeben") + "\",");
                  out.println("\"status\":\"" + rs.getString("status") + "\",");
                  out.println("\"gebuehr\":" + rs.getDouble("gebuehr") + ",");
                  out.println("\"fehlTage\":" + rs.getInt("fehl_tage"));

                  out.println("}");
                 
                    first = false;
                }

                out.print("]");
            }

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }
}

