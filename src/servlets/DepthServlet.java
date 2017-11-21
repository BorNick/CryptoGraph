package servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DepthServlet extends HttpServlet {

  public void doGet(HttpServletRequest request,HttpServletResponse response)
    throws ServletException, IOException {

    PrintWriter out = response.getWriter();
	try{
		Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection ("jdbc:mysql://localhost:3306/trading2", "root", "");
		String query;
		PreparedStatement preparedStatement;
		if(request.getParameter("timestamp") != null && request.getParameter("pair") != null && request.getParameter("stock") != null){
			query = "SELECT crtime, data FROM depth WHERE crtime >= ? AND pair = ? AND stock = ? ORDER BY crtime ASC LIMIT 1";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setLong(1, Long.decode(request.getParameter("timestamp")));
			preparedStatement.setInt(2, Integer.decode(request.getParameter("pair")));
			preparedStatement.setInt(3, Integer.decode(request.getParameter("stock")));
		}else{
			query = "SELECT crtime, data FROM depth ORDER BY crtime DESC LIMIT 1";
			preparedStatement = connection.prepareStatement(query);
		}
		//Statement stmt = connection.createStatement();
		//ResultSet rs = stmt.executeQuery(query);
		ResultSet rs = preparedStatement.executeQuery();
		if(!rs.next()){
			query = "SELECT crtime, data FROM depth WHERE crtime <= ? AND pair = ? AND stock = ? ORDER BY crtime DESC LIMIT 1";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setLong(1, Long.decode(request.getParameter("timestamp")));
			preparedStatement.setInt(2, Integer.decode(request.getParameter("pair")));
			preparedStatement.setInt(3, Integer.decode(request.getParameter("stock")));
			rs = preparedStatement.executeQuery();
			rs.next();
		}
		java.util.Date date = new java.util.Date(rs.getLong("crtime") * 1000);
		String data = rs.getString("data");
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(data);
		JsonNode orders = rootNode.path("asks");
		Iterator<JsonNode> elements = orders.elements();
		double bestAsk = 0;
		if(elements.hasNext()){
			JsonNode order = elements.next();
			bestAsk = order.path("price").asDouble();
		}
		orders = rootNode.path("bids");
		elements = orders.elements();
		double bestBid = 0;
		if(elements.hasNext()){
			JsonNode order = elements.next();
			bestBid = order.path("price").asDouble();
		}
		double middle = (bestAsk + bestBid) / 2;
		double step = 10;
		double k = 0.3;
		double asksBorder = ((1 + k) * middle);
		asksBorder += step - (asksBorder % step);
		double bidsBorder = ((1 - k) * middle);
		bidsBorder -= bidsBorder % step;
		orders = rootNode.path("asks");
		elements = orders.elements();
		double minAsks = 0;
		double curPrice = 0;
		boolean first = true;
		boolean firstPoint = true;
		double stepVolume = 0;
		String asksJson = "";
		while (curPrice <= asksBorder && elements.hasNext()){
			JsonNode order = elements.next();
			double volume = order.path("amount").asDouble();
			double price = order.path("price").asDouble();
			if(first){
				minAsks = curPrice = price - price%step;
				first = false;
			}
			if(curPrice + step < price){
				while(curPrice + step < price){
					curPrice += step;
					if(firstPoint){
						asksJson = asksJson + stepVolume;
						firstPoint = false;
					}else
						asksJson = asksJson + ", " + stepVolume;
					//stepVolume = 0;
				}
			}
			stepVolume += volume;
		}
		double maxAsks = curPrice;
		
		orders = rootNode.path("bids");
		elements = orders.elements();
		double maxBids = 0;
		curPrice = bidsBorder + 1;
		first = true;
		firstPoint = true;
		stepVolume = 0;
		String bidsJson = "";
		while (curPrice >= bidsBorder && elements.hasNext()){
			JsonNode order = elements.next();
			double volume = order.path("amount").asDouble();
			double price = order.path("price").asDouble();
			if(first){
				maxBids = curPrice = price - price%step;
				first = false;
			}
			if(curPrice >= price){
				while(curPrice >= price){
					curPrice -= step;
					if(firstPoint){
						bidsJson = stepVolume + bidsJson;
						firstPoint = false;
					}else
						bidsJson = stepVolume + ", " + bidsJson;
					//stepVolume = 0;
				}
				//System.out.println(curPrice);
			}
			stepVolume += volume;
		}
		double minBids = curPrice;
		for(double i = maxBids; i < maxAsks; i += step){
			bidsJson += ", 0";
		}
		for(double i = minAsks - step; i >= minBids; i -= step){
			asksJson = "0, " + asksJson;
		}
	    String json = "[\n\"" + date + "\",\n" + minBids + ",\n[" + asksJson + "],\n[" + bidsJson + "]\n]";
		
		//System.out.println("maxAsks " + maxAsks + "\nminAsks " + minAsks + "\nmaxBids " + maxBids + "\nminBids " + minBids);
	    out.println(json);
	    //System.out.println(json);
	} catch (ClassNotFoundException e) {
        System.out.println("ClassNotFoundException" + e.getMessage());
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
  }
}
