package servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class ControlPanelServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,HttpServletResponse response)
    throws ServletException, IOException {

    PrintWriter out = response.getWriter();
	out.println("<html>");
	HttpSession session = request.getSession(false);
	String check = "";
	if (true || session != null) {//temp
		out.println("<body>");
        check = (String) session.getAttribute("check");
        if (true) {//temp
			if(request.getParameter("action") != null){
				out.print(request.getParameter("action"));
			}else{
				request.getRequestDispatcher("controlpanel.html").include(request, response);
			}
        }else{
            out.print("You don't have permission to view this page.");
        }
    } else {
		out.println("<body onload=\"alert('Please, log in first.');\">");
        request.getRequestDispatcher("login.html").include(request, response);
    }

    out.println("</body></html>");
    out.close();
  }
}
