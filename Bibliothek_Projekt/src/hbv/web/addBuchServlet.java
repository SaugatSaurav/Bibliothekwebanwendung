package hbv.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;


import java.sql.*;
import javax.sql.*;
import javax.naming.*;

public class addBuchServlet extends HttpServlet{

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
 
    String Titel = request.getParameter("titel");
    String Autor= request.getParameter("autor");
    String isbn= request.getParameter("isbn");
    long ISBN=Long.parseLong(isbn);
    String Jahr= request.getParameter("jahr");
    java.sql.Date datum = java.sql.Date.valueOf(Jahr);
    String Genre= request.getParameter("genre");
    

       HttpSession session = request.getSession(false);
        if (session==null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }

   
        try {
        Context initCtx = new InitialContext();
        DataSource ds = (DataSource)initCtx.lookup("java:/comp/env/jdbc/mariadb");
        
        PrintWriter pw= response.getWriter();

        try(Connection connection = ds.getConnection()){ 
         String query="select * from buch where isbn=?";

         try(PreparedStatement ps=connection.prepareStatement(query)){
         ps.setLong(1,ISBN);

         ResultSet rs=ps.executeQuery();
          if(rs.next()){

            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.setContentType("text/plain");
            pw.println("Diese ISBN existiert schon!");
            return;

         }
       }




       
        
      String Query="insert into buch(titel,autor,isbn,erscheinungsjahr,genre) values (?,?,?,?,?)";  

     PreparedStatement ps = connection.prepareStatement(Query, Statement.RETURN_GENERATED_KEYS);
     ps.setString(1,Titel);
     ps.setString(2,Autor);
     ps.setLong(3,ISBN);
     ps.setDate(4,datum);
     ps.setString(5,Genre);
     


     int count= ps.executeUpdate();
    
     
  
     if(count>0){
      
       //Geänderte

       ResultSet keys = ps.getGeneratedKeys();

    if(keys.next()){

        int buchId = keys.getInt(1);

      //  HttpSession session = request.getSession(false);

        int managementId = (Integer) session.getAttribute("management_id");
        String rolle = (String) session.getAttribute("rolle");

        boolean sichtbarFuerAlle = "Manager".equals(rolle);

        String sichtQuery =
        "INSERT INTO buch_sichtbarkeit(buch_id, verwaltung_id, sichtbar_fuer_alle) VALUES (?, ?, ?)";

        PreparedStatement ps2 = connection.prepareStatement(sichtQuery);

        ps2.setInt(1, buchId);
        ps2.setInt(2, managementId);
        ps2.setBoolean(3, sichtbarFuerAlle);

        ps2.executeUpdate();
        ps2.close();
    }

      //Bis hier Geänderte 
    
        response.setStatus(HttpServletResponse.SC_OK);
   
    }
     ps.close();
     }
    
}catch(SQLException | NamingException e){
  e.printStackTrace();
  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

}
     
}}
