package com.Madhavi;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public MyServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String inputData = request.getParameter("userinput");

		// API key setup
		String apiKey = "58586080cd4a9a7a5011a37aee951ca1";

		// Get city and encode it
		String city = request.getParameter("city");
		String cityEncoded = URLEncoder.encode(city, "UTF-8");

		// Build API URL
		String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + cityEncoded + "&appid=" + apiKey + "&units=metric";

		// Create connection
		URL url = new URL(apiUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		int responseCode = connection.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			// Read response
			InputStream inputStream = connection.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream);
			Scanner scanner = new Scanner(reader);
			StringBuilder responseContent = new StringBuilder();

			while (scanner.hasNext()) {
				responseContent.append(scanner.nextLine());
			}
			scanner.close();

			// Parse JSON
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);
			
			//Date & Time
            long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
            String date = new Date(dateTimestamp).toString();
            
            //Temperature
            double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            int temperatureCelsius = (int) (temperatureKelvin - 273.15);
           
            //Humidity
            int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
            
            //Wind Speed
            double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
            
            //Weather Condition
            String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
            
            // Set the data as request attributes (for sending to the jsp page)
            request.setAttribute("date", date);
            request.setAttribute("city", city);
            request.setAttribute("temperature", temperatureCelsius);
            request.setAttribute("weatherCondition", weatherCondition); 
            request.setAttribute("humidity", humidity);    
            request.setAttribute("windSpeed", windSpeed);
            request.setAttribute("weatherData", responseContent.toString());
            
            connection.disconnect();
         // Forward the request to the weather.jsp page for rendering
            request.getRequestDispatcher("index.jsp").forward(request, response);

			// Extract weather data
			JsonObject main = jsonObject.getAsJsonObject("main");
			double temp = main.get("temp").getAsDouble();

			JsonObject weather = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject();
			String description = weather.get("description").getAsString();

			// Display response
			response.setContentType("text/html");
			response.getWriter().println("<h3>Weather in " + city + ":</h3>");
			response.getWriter().println("<p>Temperature: " + temp + " °C</p>");
			response.getWriter().println("<p>Description: " + description + "</p>");

		} else {
			// Handle error
			response.setContentType("text/html");
			response.getWriter().println("<h3>Error fetching weather data. HTTP Code: " + responseCode + "</h3>");
		}
	}
}
