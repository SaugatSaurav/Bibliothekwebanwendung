package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class MitarbeiterLoeschenServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter pw = response.getWriter();

        String Id = request.getParameter("id");
        int mitarbeiterid=Integer.parseInt(Id);

        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");

            Connection connection = ds.getConnection();

               
              String Query = "DELETE FROM Verwaltung WHERE id = ?";


                PreparedStatement ps = connection.prepareStatement(Query);
                    ps.setInt(1, mitarbeiterid);
                                   

              
                    int count = ps.executeUpdate();

                    if (count > 0) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        pw.println("Mitarbeiter wurde erfolgreich gelöscht.");
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        pw.println("Fehler beim Löschen des Mitarbeiters!");
                    }
               
            

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            pw.println("Serverfehler beim Löschen.");
        }
    }
}
