package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;


import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class ExemplarLoeschenServlet extends HttpServlet{

protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
 
    
    String Id= request.getParameter("id");
    int exemplarId=Integer.parseInt(Id);
        
        try {
    

        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
        
        PrintWriter pw= response.getWriter();
        try(Connection connection = ds.getConnection()){

         String query =
                    "DELETE FROM reservierung WHERE exemplar_id = ?";

                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, exemplarId);
                    ps.executeUpdate();
                }
       



         String Query = "DELETE FROM exemplar WHERE id=?";

          try(PreparedStatement ps = connection.prepareStatement(Query)){

                    ps.setInt(1, exemplarId);

                  int count=  ps.executeUpdate();
                

                       if (count > 0) {

                    response.setStatus(HttpServletResponse.SC_OK);

                        pw.println("Exemplar wurde erfolgreich gelöscht.");

                    } else {

                       pw.println("Fehler beim Löschen");
                    }
          }
        }
                   
       } catch (SQLException | NamingException e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

         }
    }
}
