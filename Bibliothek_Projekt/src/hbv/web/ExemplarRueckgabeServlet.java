package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class ExemplarRueckgabeServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter pw = response.getWriter();

       
         HttpSession session = request.getSession(false);
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                pw.println("Sie sind nicht eingeloggt!");
                return;
            }

            Integer mitgliedId = (Integer) session.getAttribute("Mitglied_id");

            String reservierungIdStr = request.getParameter("reservierungId");
            int reservierungId = Integer.parseInt(reservierungIdStr);

                    
            try{
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            try (Connection connection = ds.getConnection()) {
              
                  String query = """
                     UPDATE reservierung SET status = 'RUECKGABE_ANGEFRAGT'
                     WHERE id = ? AND mitglied_id = ? AND status = 'AUSGELIEHEN'
                      """;

                try (PreparedStatement ps = connection.prepareStatement(query)) {

                    ps.setInt(1, reservierungId);
                    ps.setInt(2, mitgliedId);

                    int count = ps.executeUpdate();

                    if (count > 0) {

                        response.setStatus(HttpServletResponse.SC_OK);
                        pw.println("Rückgabe wurde angefragt.");

                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        pw.println("Keine gültige Ausleihe gefunden.");
                    }
                }
            }

        } catch (SQLException | NamingException e) {

            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            pw.println("Datenbankfehler.");
        }
    }
}
