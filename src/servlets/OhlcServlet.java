package servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class OhlcServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,HttpServletResponse response)
    throws ServletException, IOException {

    PrintWriter out = response.getWriter();
	try{
		Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection ("jdbc:mysql://localhost:3306/trading2", "root", "");
		String query;
		PreparedStatement preparedStatement;
		if(request.getParameter("timestamp") != null && request.getParameter("pair") != null && request.getParameter("stock") != null){
			query = "SELECT * FROM candlestick WHERE timestamp >= ? AND pair = ? AND stock = ? ORDER BY timestamp ASC";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setLong(1, Long.decode(request.getParameter("timestamp")));
			preparedStatement.setInt(2, Integer.decode(request.getParameter("pair")));
			preparedStatement.setInt(3, Integer.decode(request.getParameter("stock")));
		}else{
			query = "SELECT * FROM candlestick ORDER BY timestamp ASC";
			preparedStatement = connection.prepareStatement(query);
		}
		//Statement stmt = connection.createStatement();
		//ResultSet rs = stmt.executeQuery(query);
		ResultSet rs = preparedStatement.executeQuery();
	    String json = "[\n";
		boolean first = true;
		while(!rs.isLast()){
			rs.next();
			if(first){
				first = false;
			}else{
				json += ",\n";
			}
			json += "[" + rs.getString("timestamp") + "000," + 
			rs.getString("open") + "," + 
			rs.getString("high") + "," + 
			rs.getString("low") + "," + 
			rs.getString("close") + "," + 
			rs.getString("volume") + "]";
		}
		json += "\n]";
	    out.println(json);
	    //System.out.println(json);
	} catch (ClassNotFoundException e) {
        System.out.println("ClassNotFoundException" + e.getMessage());
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
  }
}
