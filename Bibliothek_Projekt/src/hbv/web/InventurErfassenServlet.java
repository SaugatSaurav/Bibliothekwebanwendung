package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class InventurErfassenServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("management_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }


        int managerId = (Integer) session.getAttribute("management_id");
        int inventurExemplarId = Integer.parseInt(request.getParameter("inventurExemplarId"));
        String neuerZustand = request.getParameter("status");

        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
            Connection connection = ds.getConnection();

            // inventur_exemplar aktualisieren
            String updateInventur =
                "UPDATE inventur_exemplar SET status_neu = ?, erfasst_am = CURRENT_TIMESTAMP, erfasst_von = ? WHERE id = ?";
            PreparedStatement ps1 = connection.prepareStatement(updateInventur);
            ps1.setString(1, neuerZustand);
            ps1.setInt(2, managerId);
            ps1.setInt(3, inventurExemplarId);
            ps1.executeUpdate();
            ps1.close();

            //  exemplar Tabelle aktualisieren - zustand und verfuegbar
            // Bei VERMISST und BESCHÄDIGT verfuegbar = 2, bei allem anderen: verfuegbar = 1
            int verfuegbar = "VERMISST".equals(neuerZustand) || "BESCHAEDIGT".equals(neuerZustand) ? 2 : 1;

            String updateExemplar =
                "UPDATE exemplar e JOIN inventur_exemplar ie ON e.id = ie.exemplar_id " +
                "SET e.zustand = ?, e.verfuegbar = ? WHERE ie.id = ?";

            PreparedStatement ps2 = connection.prepareStatement(updateExemplar);
            ps2.setString(1, neuerZustand);  // NEU, GUT, BESCHAEDIGT oder VERMISST
            ps2.setInt(2, verfuegbar);
            ps2.setInt(3, inventurExemplarId);
            ps2.executeUpdate();
            ps2.close();

            connection.close();

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
