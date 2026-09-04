package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class InventurListeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("management_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            pw.print("[]");
            return;
        }

        String rolle = (String) session.getAttribute("rolle");
        if (!"Manager".equals(rolle)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            pw.print("[]");
            return;
        }

        String inventurId = request.getParameter("inventurId");

        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
            Connection connection = ds.getConnection();

            PreparedStatement ps;
            ResultSet rs;

            if (inventurId != null && !inventurId.isEmpty()) {
                // Details einer bestimmten Inventur
                String query =
                    "SELECT ie.id, ie.exemplar_id, ie.status_alt, ie.status_neu, ie.erfasst_am, " +
                    "e.barcode, e.regal, e.zustand, b.titel, b.autor, s.name AS standort " +
                    "FROM inventur_exemplar ie " +
                    "JOIN exemplar e ON ie.exemplar_id = e.id " +
                    "JOIN buch b ON e.buch_id = b.id " +
                    "LEFT JOIN standort s ON e.standort_id = s.id " +
                    "WHERE ie.inventur_id = ?";
                ps = connection.prepareStatement(query);
                ps.setInt(1, Integer.parseInt(inventurId));
                rs = ps.executeQuery();

                pw.print("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) pw.print(",");
                    pw.print("{");
                    pw.print("\"id\":" + rs.getInt("id") + ",");
                    pw.print("\"exemplarId\":" + rs.getInt("exemplar_id") + ",");
                    pw.print("\"titel\":\"" + rs.getString("titel") + "\",");
                    pw.print("\"autor\":\"" + rs.getString("autor") + "\",");
                    pw.print("\"barcode\":\"" + rs.getString("barcode") + "\",");
                    pw.print("\"standort\":\"" + rs.getString("standort") + "\",");
                    pw.print("\"regal\":\"" + rs.getString("regal") + "\",");
                    pw.print("\"zustand\":\"" + rs.getString("zustand") + "\",");
                    pw.print("\"statusAlt\":\"" + rs.getString("status_alt") + "\",");
                    pw.print("\"statusNeu\":" + (rs.getString("status_neu") != null ? "\"" + rs.getString("status_neu") + "\"" : "null") + ",");
                    pw.print("\"erfasstAm\":\"" + rs.getString("erfasst_am") + "\"");
                    pw.print("}");
                    first = false;
                }
                pw.print("]");

            } else {
                // Alle Inventuren mit Statistik
                String query =
                    "SELECT i.id, i.standort_id, i.start_datum, i.end_datum, i.status, s.name AS standort_name, " +
                    "COUNT(ie.id) AS total_exemplare, " +
                    "SUM(CASE WHEN ie.status_neu IS NOT NULL THEN 1 ELSE 0 END) AS erfasst_exemplare, " +
                    "SUM(CASE WHEN ie.status_neu = 'VERMISST' THEN 1 ELSE 0 END) AS vermisst, " +
                    "SUM(CASE WHEN ie.status_neu = 'BESCHAEDIGT' THEN 1 ELSE 0 END) AS beschadigt, " +
                    "SUM(CASE WHEN ie.status_neu = 'NEU' THEN 1 ELSE 0 END) AS neu, " +
                    "SUM(CASE WHEN ie.status_neu = 'GUT' THEN 1 ELSE 0 END) AS gut " +
                    "FROM inventur i " +
                    "LEFT JOIN inventur_exemplar ie ON i.id = ie.inventur_id " +
                    "JOIN standort s ON i.standort_id = s.id " +
                    "GROUP BY i.id ORDER BY i.start_datum DESC";
                ps = connection.prepareStatement(query);
                rs = ps.executeQuery();

                pw.print("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) pw.print(",");
                    int total = rs.getInt("total_exemplare");
                    int erfasst = rs.getInt("erfasst_exemplare");
                    int vermisst = rs.getInt("vermisst");
                    int beschadigt = rs.getInt("beschadigt");
                    int neu = rs.getInt("neu");
                    int gut = rs.getInt("gut");
                    String status = rs.getString("status");

                    String statusText = "Laufend";
                    if ("ABGESCHLOSSEN".equals(status)) {
                        statusText = "Abgeschlossen";
                    } else if ("ABGEBROCHEN".equals(status)) {
                        statusText = "Abgebrochen";
                    }

                    pw.print("{");
                    pw.print("\"id\":" + rs.getInt("id") + ",");
                    pw.print("\"standort\":\"" + rs.getString("standort_name") + "\",");
                    pw.print("\"startDatum\":\"" + rs.getString("start_datum") + "\",");
                    pw.print("\"endDatum\":\"" + (rs.getString("end_datum") != null ? rs.getString("end_datum") : "-") + "\",");
                    pw.print("\"status\":\"" + status + "\",");
                    pw.print("\"statusText\":\"" + statusText + "\",");
                    pw.print("\"totalExemplare\":" + total + ",");
                    pw.print("\"erfasstExemplare\":" + erfasst + ",");
                    pw.print("\"vermisst\":" + vermisst + ",");
                    pw.print("\"beschadigt\":" + beschadigt + ",");
                    pw.print("\"neu\":" + neu + ",");
                    pw.print("\"gut\":" + gut);
                    pw.print("}");
                    first = false;
                }
                pw.print("]");
            }

            rs.close();
            ps.close();
            connection.close();

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            pw.print("[]");
        }
    }
}
