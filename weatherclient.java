// Source code is decompiled from a .class file using FernFlower decompiler.
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherClient {
   public WeatherClient() {
   }

   public static void main(String[] args) {
      // Set DNS servers for better connectivity
      System.setProperty("sun.net.spi.nameservice.nameservers", "8.8.8.8,8.8.4.4");
      System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");

      try (Scanner scanner = new Scanner(System.in)) {
         // Ask user if they want to check weather
         System.out.print("Would you like to see the current weather? (yes/no): ");
         String response = scanner.nextLine().trim().toLowerCase();
         
         if (!response.equals("yes") && !response.equals("y")) {
            System.out.println("Okay, have a great day!");
            return;
         }

         // Get location from user
         System.out.print("Enter the name of the location (e.g., London): ");
         String location = scanner.nextLine().trim();
         
         if (location.isEmpty() || location.matches(".*[^a-zA-Z0-9\\s].*")) {
            System.out.println("Invalid location name. Please use alphanumeric characters and spaces only.");
            return;
         }

         // Get coordinates using geocoding API
         double latitude = 0.0;
         double longitude = 0.0;
         boolean locationFound = false;
         
         try {
            String geocodingUrl = String.format(
               "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=en&format=json",
               URLEncoder.encode(location, "UTF-8")
            );
            System.out.println("Searching for location: " + location);

            // Try up to 3 times to get location data
            for (int attempt = 0; attempt < 3 && !locationFound; attempt++) {
               HttpURLConnection connection = null;
               try {
                  URL url = new URL(geocodingUrl);
                  connection = (HttpURLConnection) url.openConnection();
                  connection.setRequestMethod("GET");
                  connection.setRequestProperty("User-Agent", "WeatherClient/1.0");
                  connection.setConnectTimeout(15000);
                  connection.setReadTimeout(15000);

                  int responseCode = connection.getResponseCode();
                  System.out.println("Geocoding API response code: " + responseCode);

                  if (responseCode == 200) {
                     // Read the response
                     BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                     StringBuilder response2 = new StringBuilder();
                     String line;

                     while ((line = reader.readLine()) != null) {
                        response2.append(line);
                     }
                     reader.close();

                     String jsonResponse = response2.toString();
                     System.out.println("Geocoding response: " + jsonResponse);

                     // Parse JSON response
                     JSONObject jsonObject = new JSONObject(jsonResponse);
                     if (jsonObject.has("results") && jsonObject.getJSONArray("results").length() > 0) {
                        JSONObject result = jsonObject.getJSONArray("results").getJSONObject(0);
                        
                        if (result.has("latitude") && result.has("longitude")) {
                           latitude = result.getDouble("latitude");
                           longitude = result.getDouble("longitude");
                           String name = result.getString("name");
                           String country = result.optString("country", "");
                           
                           System.out.println("\nFound location: " + name + ", " + country);
                           System.out.println("\nLatitude: " + latitude + ", Longitude: " + longitude);
                           locationFound = true;
                           break;
                        } else {
                           System.out.println("Invalid geocoding response: Missing latitude or longitude.");
                           return;
                        }
                     } else {
                        System.out.println("Location '" + location + "' not found. Please check the spelling or try a different city name.");
                        return;
                     }
                  } else {
                     // Handle error response
                     BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                     StringBuilder errorResponse = new StringBuilder();
                     String line;

                     while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                     }
                     reader.close();

                     System.out.println("Error response: " + errorResponse.toString());
                     
                     if (attempt < 2) {
                        System.out.println("Retrying... (" + (attempt + 2) + "/3)");
                        Thread.sleep(1000 * (attempt + 1));
                     }
                  }
               } catch (Exception e) {
                  System.out.println("Error during geocoding attempt " + (attempt + 1) + ": " + e.getMessage());
                  if (attempt < 2) {
                     Thread.sleep(1000 * (attempt + 1));
                  }
               } finally {
                  if (connection != null) {
                     connection.disconnect();
                  }
               }
            }

            if (!locationFound) {
               System.out.println("Failed to fetch geocoding data after 3 attempts.");
               return;
            }

            // Simulate weather data (demo mode)
            System.out.println("Fetching weather data...");
            Thread.sleep(1000);

            // Create mock weather data
            String mockWeatherData = String.format(
               "{\"current_condition\":[{\"temp_C\":\"%d\",\"FeelsLikeC\":\"%d\",\"humidity\":\"%d\",\"windspeedKmph\":\"%d\",\"winddir16Point\":\"%s\",\"weatherDesc\":\"%s\",\"observation_time\":\"%s\"}]}",
               25 + (int)(Math.random() * 15),
               23 + (int)(Math.random() * 15),
               40 + (int)(Math.random() * 40),
               5 + (int)(Math.random() * 20),
               "NE",
               "Partly cloudy",
               "12:00 PM"
            );

            System.out.println("Weather API response: " + mockWeatherData);

            // Parse weather data
            JSONObject weatherJson = new JSONObject(mockWeatherData);
            if (weatherJson.has("current_condition") && weatherJson.getJSONArray("current_condition").length() > 0) {
               JSONObject currentCondition = weatherJson.getJSONArray("current_condition").getJSONObject(0);
               
               // Verify all required fields are present
               String[] requiredFields = {
                  "temp_C", "FeelsLikeC", "humidity", "windspeedKmph",
                  "winddir16Point", "weatherDesc", "observation_time"
               };
               
               for (String field : requiredFields) {
                  if (!currentCondition.has(field)) {
                     throw new JSONException("Missing required field: " + field);
                  }
               }

               // Display weather information
               System.out.println("\nCurrent Weather for " + location + ":");
               System.out.println("\nTemperature: " + currentCondition.getString("temp_C") + " °C");
               System.out.println("\nFeels Like: " + currentCondition.getString("FeelsLikeC") + " °C");
               System.out.println("\nHumidity: " + currentCondition.getString("humidity") + "%");
               System.out.println("\nWind Speed: " + currentCondition.getString("windspeedKmph") + " km/h");
               System.out.println("\nWind Direction: " + currentCondition.getString("winddir16Point"));
               System.out.println("\nWeather Description: " + currentCondition.getString("weatherDesc"));
               System.out.println("\nTime: " + currentCondition.getString("observation_time"));

               // Display demo mode notice
               System.out.println("\n" + "=".repeat(50));
               System.out.println("DEMO MODE - This shows how the REST API client works!");
               System.out.println("In a real environment with network access, this would:");
               System.out.println("1. Make HTTP request to weather API");
               System.out.println("2. Parse JSON response");
               System.out.println("3. Display structured weather data");
               System.out.println("=".repeat(50));
            } else {
               throw new JSONException("Invalid weather response: Missing or empty current_condition array");
            }

         } catch (Exception e) {
            System.out.println("Error during geocoding: " + e.getMessage());
            e.printStackTrace();
         }
      } catch (Exception e) {
         System.out.println("Unexpected error: " + e.getMessage());
         e.printStackTrace();
      }
   }
}
