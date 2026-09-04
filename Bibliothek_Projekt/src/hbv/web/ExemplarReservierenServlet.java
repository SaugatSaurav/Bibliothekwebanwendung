package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class ExemplarReservierenServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/plain;charset=UTF-8");

          String exemplarid = request.getParameter("exemplarid");
          int exemplarId = Integer.parseInt(exemplarid);

          HttpSession session = request.getSession(false);

          int mitgliedId = (Integer) session.getAttribute("Mitglied_id");

            try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try (Connection connection = ds.getConnection()) {

                String query =
                    "INSERT INTO reservierung(exemplar_id, mitglied_id, zurueckgeben) " +
                    "VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL 4 WEEK))";
                  //  "VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL 1 DAY))";

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, exemplarId);
                    ps.setInt(2, mitgliedId);
                    ps.executeUpdate();
                }

                
                  String Query=  "UPDATE exemplar SET verfuegbar = 3 " +
                    "WHERE id = ? AND verfuegbar = 1";

                try (PreparedStatement ps = connection.prepareStatement(Query)) {
                    ps.setInt(1, exemplarId);

                    int count = ps.executeUpdate();

                    if (count > 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println("Reserviert");
                    } else {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        response.getWriter().println("Dieses Exemplar ist nicht mehr verfügbar.");
                    }
                }
            }

        } catch (SQLException | NamingException | NumberFormatException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
