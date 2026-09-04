package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class InventurAbschliessenServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter pw = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("management_id") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            pw.print("Nicht eingeloggt");
            return;
        }

        int inventurId = Integer.parseInt(request.getParameter("inventurId"));

        try {
            Context initCtx = new InitialContext();
            DataSource ds = (DataSource) initCtx.lookup("java:/comp/env/jdbc/mariadb");
            Connection connection = ds.getConnection();

            String update = "UPDATE inventur SET status = 'ABGESCHLOSSEN', end_datum = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(update);
            ps.setInt(1, inventurId);
            ps.executeUpdate();
            ps.close();
            connection.close();


        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
