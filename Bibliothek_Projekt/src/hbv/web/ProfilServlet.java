package hbv.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.*;

public class ProfilServlet extends HttpServlet {

    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if(session == null){
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
         return;
     }

     
        response.setContentType(
            "application/json");

        PrintWriter out =
            response.getWriter();

        out.print("{");

        if(session.getAttribute("Mitglied_id") != null){

        out.print("\"id\":\"" + session.getAttribute("Mitglied_id") + "\",");
        out.print("\"name\":\"" + session.getAttribute("username_mitglied") + "\",");
        out.print("\"email\":\"" + session.getAttribute("email_mitglied") + "\",");
        out.print("\"stat\":\"" + session.getAttribute("status_mitglied") + "\",");
        out.print("\"addresse\":\"" + session.getAttribute("addresse_mitglied") + "\",");
        out.print("\"datum\":\"" + session.getAttribute("datum_mitglied") + "\"");

   }else{

        out.print("\"id\":\""
            + session.getAttribute("management_id")
            + "\",");

        out.print("\"vorname\":\""
            + session.getAttribute("vorname")
            + "\",");

        out.print("\"nachname\":\""
            + session.getAttribute("nachname")
            + "\",");

        out.print("\"email\":\""
            + session.getAttribute("email")
            + "\",");

        out.print("\"rolle\":\""
            + session.getAttribute("rolle")
            + "\",");

        out.print("\"addresse\":\""
            + session.getAttribute("addresse")
            + "\",");

        out.print("\"standort\":\""
            + session.getAttribute("standortName")
            + "\"");
   }
        out.print("}");
    }
}
