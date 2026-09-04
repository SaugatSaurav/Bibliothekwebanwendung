package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class BuchLoeschenServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter pw = response.getWriter();

        String Id = request.getParameter("id");
        int buchid=Integer.parseInt(Id);

        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try (Connection connection = ds.getConnection()) {

                String deleteReservierung =
                    "DELETE r FROM reservierung r " +
                    "JOIN exemplar e ON r.exemplar_id = e.id " +
                    "WHERE e.buch_id = ?";

                try (PreparedStatement ps = connection.prepareStatement(deleteReservierung)) {
                    ps.setInt(1, buchid);
                    ps.executeUpdate();
                }


               
              String Query = "DELETE FROM exemplar WHERE buch_id = ?";


                try (PreparedStatement ps = connection.prepareStatement(Query)) {
                    ps.setInt(1, buchid);
                    ps.executeUpdate();
                }

                String qquery = "DELETE FROM buch_sichtbarkeit WHERE buch_id=?";

                try (PreparedStatement ps = connection.prepareStatement(qquery)) {
                    ps.setInt(1, buchid);
                    ps.executeUpdate();
                }





                String query = "DELETE FROM buch WHERE id = ?";

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, buchid);

                    int count = ps.executeUpdate();

                    if (count > 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        pw.println("Buch und alle Exemplare wurden erfolgreich gelöscht.");
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        pw.println("Fehler beim Löschen des Buches.");
                    }
                }
            }

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            pw.println("Serverfehler beim Löschen.");
        }
    }
}
