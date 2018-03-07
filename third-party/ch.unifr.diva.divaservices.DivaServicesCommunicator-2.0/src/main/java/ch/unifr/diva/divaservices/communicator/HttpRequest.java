package ch.unifr.diva.divaservices.communicator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author 317617032205
 */
public class HttpRequest {

    public static JSONObject executeGet(String url) throws ProtocolException, MalformedURLException, IOException {
        // HttpClient client = new DefaultHttpClient(); // HttpURLConnection instead
        // HttpGet get = new HttpGet(url);
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        // add request header
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
        connection.setRequestProperty("Accept", "*/*");

        String connectionStr = convertStreamToString(connection);
        JSONObject connectionJO = new JSONObject(connectionStr);

        return connectionJO;
    }

    // Does executePost work?
    public static JSONObject executePost(String url, JSONObject payload) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestMethod("POST");
        
        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
        wr.write(payload.toString());
        wr.flush();

        String connectionStr = convertStreamToString(connection);
        JSONObject connectionJO = new JSONObject(connectionStr);

        return connectionJO;

    }

    private static String convertStreamToString(HttpURLConnection connection) throws IOException {
        //InputStream instream = entity.getContent();
        BufferedReader in = null;
        if (connection.getResponseCode() > 400 && connection.getResponseCode() < 600) {
            in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

        } else {
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine + "\n");
        }
        in.close();

        return response.toString();

    }

    /**
     * Gets a result JSON object from the exeuction response This method will
     * run GET requests in a 5 second interval until the result is available
     *
     * @param result The JSON object return from the POST request
     * @param checkInterval How often to check for new results (in seconds)
     * @return The result JSON object
     */
    public static List<JSONObject> getResult(JSONObject result, int checkInterval) throws IOException {
        JSONArray results = result.getJSONArray("results");
        List<JSONObject> response = new LinkedList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject res = results.getJSONObject(i);
            String url = res.getString("resultLink");
            JSONObject getResult = getSingleResult(url, checkInterval);
            response.add(getResult);
        }
        return response;
    }

    private static JSONObject getSingleResult(String url, int checkInterval) throws MalformedURLException, IOException {
        JSONObject getResult = executeGet(url);
        while (!getResult.getString("status").equals("done")) {  // HERE THE PROGRAM GETS STUCK!!!! STATUS IS NEVER EQUAL DONE. WHY??
            //Result not available yet
            try {
                //Wait 5 seconds and try again
                Thread.sleep(checkInterval * 1000);
                getResult = executeGet(url);
               // System.out.println(getResult + "after");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return getResult;
    }

}
