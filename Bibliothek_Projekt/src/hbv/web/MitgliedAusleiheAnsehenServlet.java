package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;

import javax.sql.*;
import javax.naming.*;

public class MitgliedAusleiheAnsehenServlet extends HttpServlet {

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("Mitglied_id") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int mitgliedId = (Integer) session.getAttribute("Mitglied_id");

        try {

            Context initCtx = new InitialContext();

            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
           

            Connection connection = ds.getConnection();

            BibliothekAutomatikService.aktualisiereGebuehrFehltageUndSperre(connection);

            String query =
              "SELECT r.id AS reservierung_id, e.id, b.titel AS titel, b.autor AS autor, b.genre AS genre, e.barcode AS barcode, " +
                "e.regal AS regal, e.zustand AS zustand, e.verfuegbar AS verfuegbar, s.name AS standort, " +
                "r.status AS stat, r.reserviert_am AS reserviert_am, r.zurueckgeben AS zurueckgeben , r.gebuehr AS gebuehr, r.fehl_tage AS fehl_tage " +
                "FROM reservierung r "+
                "JOIN exemplar e ON r.exemplar_id = e.id " +
                "JOIN buch b ON e.buch_id = b.id " +
                "JOIN standort s ON e.standort_id = s.id " +
                "WHERE r.mitglied_id = ? AND r.status IN ('AUSGELIEHEN', 'RUECKGABE_ANGEFRAGT') " +
                "AND e.verfuegbar = 0 AND e.zustand IN ('NEU','GUT') " +
                "ORDER BY r.reserviert_am DESC";

            PreparedStatement ps = connection.prepareStatement(query);
              ps.setInt(1, mitgliedId);

            ResultSet rs = ps.executeQuery();

            PrintWriter out = response.getWriter();

             out.print("[");

             boolean first = true;

             while (rs.next()) {

                if (!first) {
                    out.print(",");
                }

              out.print("{");

              out.print("\"reservierungId\":" +rs.getInt("reservierung_id") + ",");
              out.print("\"id\":\"" + rs.getInt("id") + "\",");
              out.print("\"titel\":\"" + rs.getString("titel") + "\",");
              out.print("\"autor\":\"" + rs.getString("autor") + "\",");
              out.print("\"barcode\":\"" + rs.getString("barcode") + "\",");
              out.print("\"standort\":\"" + rs.getString("standort") + "\",");
              out.print("\"regal\":\"" + rs.getString("regal") + "\",");
              out.print("\"genre\":\"" + rs.getString("genre") + "\",");
              out.print("\"zustand\":\"" + rs.getString("zustand") + "\",");
              out.print("\"verfuegbar\":" + rs.getInt("verfuegbar") + ",");
              out.print("\"gebuehr\":" + rs.getBigDecimal("gebuehr") + ",");
              out.print("\"fehlTage\":" + rs.getInt("fehl_tage") + ",");
              out.print("\"stat\":\"" + rs.getString("stat") + "\",");
              out.print("\"reserviert_am\":\"" + rs.getDate("reserviert_am") + "\",");
              out.print("\"zurueckgeben\":\"" + rs.getDate("zurueckgeben") + "\"");
              
              out.print("}");

                first = false;
            }

            out.print("]");

            rs.close();
            ps.close();
            connection.close();

        } catch (SQLException | NamingException e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
        }
    }
}
