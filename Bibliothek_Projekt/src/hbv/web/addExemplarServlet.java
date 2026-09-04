package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;


import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class addExemplarServlet extends HttpServlet{

protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
 
    
     int buchId = Integer.parseInt(request.getParameter("buch_id"));

      //int standortId =Integer.parseInt(request.getParameter("standort_id"));

      
  
        String standort = request.getParameter("standort_id");

       Integer standortId = null;

      if (standort == null || standort.isEmpty()) {

       HttpSession session = request.getSession(false);

      if(session != null){

       standortId =(Integer) session.getAttribute("management_standort_id");

      }

    } else {
    standortId = Integer.parseInt(standort);
  }



        String regal =
            request.getParameter("regal");

        String zustand =
            request.getParameter("zustand");

        String barcode =
            request.getParameter("barcode");  

       


        PrintWriter pw= response.getWriter();
        try {
   
        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
        
        
          Connection connection = ds.getConnection();
         String query="INSERT INTO exemplar (buch_id, barcode, standort_id, regal, zustand) VALUES (?, ?, ?, ?, ?)";

              PreparedStatement ps =
                connection.prepareStatement(query);

            ps.setInt(1, buchId);
            ps.setString(2, barcode);
            ps.setInt(3, standortId);
            ps.setString(4, regal);
            ps.setString(5, zustand);


     int count= ps.executeUpdate();
    
     
  
     if(count>0){
       
   
    response.setStatus(HttpServletResponse.SC_OK);
   
  }
     ps.close();
     connection.close();
     
    
 } catch (SQLIntegrityConstraintViolationException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            pw.println("Barcode existiert bereits!");

        } catch(SQLException | NamingException e){
  e.printStackTrace();
  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

}
     
}}
