package servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class DepthGraphServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,HttpServletResponse response)
    throws ServletException, IOException {

    PrintWriter out = response.getWriter();
	request.getRequestDispatcher("md1.html").include(request, response);
	if(request.getParameter("pair") != null && request.getParameter("stock") != null)
		if(request.getParameter("timestamp") != null){
			out.print("?timestamp=" + request.getParameter("timestamp") + "&pair=" + request.getParameter("pair") + "&stock=" + request.getParameter("stock"));
		}else
			if(request.getParameter("date") != null){
				try{
					Date date = new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("date"));
					out.print("?timestamp=" + (date.getTime()/1000) + "&pair=" + request.getParameter("pair") + "&stock=" + request.getParameter("stock"));
				}catch(ParseException e){}
			}
	request.getRequestDispatcher("md2.html").include(request, response);
  }
  /*public void doPost(HttpServletRequest request,HttpServletResponse response)
    throws ServletException, IOException {

    PrintWriter out = response.getWriter();
	try{
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("date"));
		long timestamp = date.getTime()/1000;
		response.sendRedirect("/CryptoGraph/marketdepth");
	}catch(ParseException e){}
  }*/
}
