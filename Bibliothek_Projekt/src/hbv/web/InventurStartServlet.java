package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class InventurStartServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("management_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            pw.print("Sie sind nicht eingeloggt");
            return;
        }

       
        int managerId = (Integer) session.getAttribute("management_id");
        int standortId = Integer.parseInt(request.getParameter("standortId"));

        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
            Connection connection = ds.getConnection();

            // 1. Inventur erstellen
            String insertInventur = "INSERT INTO inventur (standort_id, erstellt_von, status) VALUES (?, ?, 'LAUFEND')";
            PreparedStatement ps1 = connection.prepareStatement(insertInventur, Statement.RETURN_GENERATED_KEYS);
            ps1.setInt(1, standortId);
            ps1.setInt(2, managerId);
            ps1.executeUpdate();

            ResultSet keys = ps1.getGeneratedKeys();
            keys.next();
            int inventurId = keys.getInt(1);
            ps1.close();

            // 2. Alle Exemplare des Standorts holen
            String selectExemplare = "SELECT id, verfuegbar FROM exemplar WHERE standort_id = ?";
            PreparedStatement psSelect = connection.prepareStatement(selectExemplare);
            psSelect.setInt(1, standortId);
            ResultSet rs = psSelect.executeQuery();

            // 3. Jedes Exemplar einzeln in inventur_exemplar einfügen
            String insertExemplar = "INSERT INTO inventur_exemplar (inventur_id, exemplar_id, status_alt) VALUES (?, ?, ?)";
            PreparedStatement psInsert = connection.prepareStatement(insertExemplar);

            int count = 0;
            while (rs.next()) {
                int exemplarId = rs.getInt("id");
                int verfuegbar = rs.getInt("verfuegbar");

                psInsert.setInt(1, inventurId);
                psInsert.setInt(2, exemplarId);
                psInsert.setInt(3, verfuegbar);
                psInsert.executeUpdate();
                count++;
            }

            rs.close();
            psSelect.close();
            psInsert.close();
            connection.close();

            pw.print("{\"inventurId\":" + inventurId + ",\"exemplareCount\":" + count + "}");

        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
