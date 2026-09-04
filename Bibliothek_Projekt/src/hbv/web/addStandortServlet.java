package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;


import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class addStandortServlet extends HttpServlet{

protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
 
    String Name = request.getParameter("name");
    String Strasse= request.getParameter("strasse");
    int PLZ = Integer.parseInt(request.getParameter("plz"));
    String Ort= request.getParameter("ort");

            HttpSession session = request.getSession(false);
          if(session==null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }
    
     
        try {
        // Naming Context
        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
        Connection connection=ds.getConnection();       
       


       
        
    String Query="insert into standort(name,strasse,plz,ort) values (?,?,?,?)";  

     PreparedStatement ps = connection.prepareStatement(Query);
     ps.setString(1,Name);
     ps.setString(2,Strasse);
     ps.setInt(3,PLZ);
     ps.setString(4,Ort);
     


     int count= ps.executeUpdate();
    
     
  
     if(count>0){
       
   
    response.setStatus(HttpServletResponse.SC_OK);
   

     ps.close();
     connection.close();
     }
   
}catch(SQLException | NamingException e){
  e.printStackTrace();
  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

}
     
}}
