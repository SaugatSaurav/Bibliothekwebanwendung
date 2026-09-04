package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class InventurLoeschenServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter pw = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("management_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            pw.print("Sie sind nicht eingeloggt.");
            return;
        }

      
        String alle = request.getParameter("alle");
        String inventurId = request.getParameter("inventurId");

        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try (Connection con = ds.getConnection()) {

                if ("true".equals(alle)) {
                    // Alle abgeschlossenen Inventuren löschen
                    // Erst inventur_exemplar (FK-Constraint), dann inventur
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE ie FROM inventur_exemplar ie " +
                            "JOIN inventur i ON ie.inventur_id = i.id " +
                            "WHERE i.status = 'ABGESCHLOSSEN'")) {
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM inventur WHERE status = 'ABGESCHLOSSEN'")) {
                        int count = ps.executeUpdate();
                        response.setStatus(HttpServletResponse.SC_OK);
                        pw.print(count + " Inventur(en) gelöscht.");
                    }

                } else if (inventurId != null && !inventurId.isEmpty()) {
                    // Einzelne Inventur löschen
                    int id = Integer.parseInt(inventurId);

                    // Sicherheitscheck: nur ABGESCHLOSSEN darf gelöscht werden
                    boolean istAbgeschlossen = false;
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT status FROM inventur WHERE id = ?")) {
                        ps.setInt(1, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                istAbgeschlossen = "ABGESCHLOSSEN".equals(rs.getString("status"));
                            }
                        }
                    }

                    if (!istAbgeschlossen) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        pw.print("Nur abgeschlossene Inventuren können gelöscht werden.");
                        return;
                    }

                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM inventur_exemplar WHERE inventur_id = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM inventur WHERE id = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                    pw.print("Inventur gelöscht.");

                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    pw.print("Keine gültige Anfrage.");
                }
            }
        } catch (SQLException | NamingException | NumberFormatException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            pw.print("Fehler beim Löschen.");
        }
    }
}
