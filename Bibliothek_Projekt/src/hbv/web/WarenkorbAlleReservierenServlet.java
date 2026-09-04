package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;
import java.sql.*;

import javax.naming.*;
import javax.sql.*;

public class WarenkorbAlleReservierenServlet extends HttpServlet {

    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType(
            "text/plain;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("Mitglied_id") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().println("Bitte zuerst einloggen.");

            return;
        }

        int mitgliedId =
            (Integer) session.getAttribute("Mitglied_id");

        try {

            Context initCtx = new InitialContext();

            DataSource ds =(DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try (Connection connection =
                     ds.getConnection()) {

                String query = 
                    "UPDATE reservierung " +
                    "SET status = 'RESERVIERT', " +
                    "reserviert_am =CURDATE(), " +
                    "zurueckgeben = DATE_ADD(CURDATE(), INTERVAL 4 WEEK) " +
                   // "zurueckgeben = DATE_ADD(CURDATE(), INTERVAL 1 DAY) " +

                    "WHERE mitglied_id = ? " +
                    "AND status = 'WARENKORB'";

                try (PreparedStatement ps =
                         connection.prepareStatement(query)) {

                    ps.setInt(1, mitgliedId);

                    int count = ps.executeUpdate();

                    if (count > 0) {

                        response.setStatus(
                            HttpServletResponse.SC_OK);

                        response.getWriter().println(count +"Exemplare wurden reserviert.");

                    } else {

                        response.setStatus(HttpServletResponse.SC_CONFLICT);

                        response.getWriter().println("Der Warenkorb ist leer.");
                    }
                }
            }

        } catch (SQLException | NamingException e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().println("Serverfehler beim Reservieren.");
        }
    }
}
