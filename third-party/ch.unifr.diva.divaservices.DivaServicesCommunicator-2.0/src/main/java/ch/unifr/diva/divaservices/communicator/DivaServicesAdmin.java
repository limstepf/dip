package ch.unifr.diva.divaservices.communicator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import ch.unifr.diva.divaservices.communicator.exceptions.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author 317617032205
 */
public class DivaServicesAdmin {

    // final output
    static JSONObject postRequest0 = new JSONObject();
    static JSONObject parameters = new JSONObject();
    static JSONArray data = new JSONArray();
    // number of compatible userparameters
    static int countmatch = 0;
    // name/key of a specific parameter
    static String pName;

    //Test
    public static void main(String[] args) throws MethodNotAvailableException, IOException, ForgotKeyValueObjectException, IncompatibleValueException, UserValueRequiredException, FileTypeConfusionException, IntermediatePOSTRequestResponseException, UserParametersOverloadException {
        Map<String, Object> userParameters = new HashMap<>();
        userParameters.put("sharpenLevel", 8);
        userParameters.put("inputImage", "communicatorTestCollection/e-codices_bnf-lat11641_001r_large.jpg");
        List<JSONObject> result = runMethod("http://divaservices.unifr.ch/api/v2/enhancement/sharpenenhancement/1", userParameters);

        //List<JSONObject> result = runMethod ("http://divaservices.unifr.ch/api/v2/graph/graphextraction/1", parameters);
        if (result != null) {
            System.out.println("Intermediate POST request response:");
            for (int i = 0; i < result.size(); i++) {
                logJsonObject(result.get(i));
            }
        } else {
            throw new IntermediatePOSTRequestResponseException("Intermediate POST request response failure");
        }
    }

    //Test
    public static List<JSONObject> runMethod(String url, Map<String, Object> userParameters) throws MethodNotAvailableException, IOException, ForgotKeyValueObjectException, IncompatibleValueException, UserValueRequiredException, FileTypeConfusionException, UserParametersOverloadException {
        JSONObject postRequest1 = checkParams(runGetRequest(url), userParameters);
        //return list of JSONObject
        return runPostRequest(url, postRequest1);
    }

    private static List<JSONObject> runPostRequest(String url, JSONObject postRequest1) throws IOException, MethodNotAvailableException {

        List<JSONObject> results = null;
        JSONObject postResult = HttpRequest.executePost(url, postRequest1);
        //logJsonObject(postResult);
        if (postResult.has("statusCode") && postResult.getInt("statusCode") == 202) {   //correct
            results = HttpRequest.getResult(postResult, 5); //HERE THE PROGRAM GETS STUCK...!!!
        } else if (postResult.has("statusCode") && postResult.getInt("statusCode") == 404) {  //wrong
            throw new MethodNotAvailableException("Method is not available (satusCode == 404)");
        } else {
            throw new MethodNotAvailableException("Method is not available (statusCode != 404)");
        }
        return results;
    }

    private static JSONObject runGetRequest(String url) throws MethodNotAvailableException, MalformedURLException, IOException {
        JSONObject response = HttpRequest.executeGet(url);
        if ((response.has("status") && response.getInt("status") == 404)) {
            throw new MethodNotAvailableException("This method is currently unavailable (status == 404)");
        }
        return response;
    }

    private static JSONObject checkParams(JSONObject object, Map<String, Object> userParameters) throws ForgotKeyValueObjectException, IncompatibleValueException, UserValueRequiredException, FileTypeConfusionException, UserParametersOverloadException {
        JSONArray arrayOfInputs = object.getJSONArray("input");
        for (int j = 0; j < arrayOfInputs.length(); j++) {
            //String input = arrayOfInputs.getJSONObject(j).toString(); // z.B {"file": {{...}}}
            String inputType = arrayOfInputs.getJSONObject(j).keys().next();
            //System.out.println(inputTypeName);
            JSONObject inputInfo = arrayOfInputs.getJSONObject(j).getJSONObject(inputType);
            if (inputInfo.has("userdefined") && inputInfo.getBoolean("userdefined") == true) {
                pName = inputInfo.getString("name");
                if (!userParameters.containsKey(pName)) {
                    throw new ForgotKeyValueObjectException("You forgot a <key,value> object with parameter key " + pName);
                }
                Object userValue = userParameters.get(pName);
                JSONObject pOptions = inputInfo.getJSONObject("options");
                if (inputType.equals("select")) {
                    checkSelect(pOptions, userValue);

                } else if (inputType.equals("number")) {
                    checkNumber(pOptions, userValue);

                } else if (inputType.equals("file")) {
                    checkFile(pOptions, userValue);

                } else {
                    //server exception "wrong filetype"
                    throw new FileTypeConfusionException("Server has file type confusion");
                }
            }
        }

        // JSON printing
        postRequest0.put("data", data);
        postRequest0.put("parameters", parameters);
        //System.out.println("********************************");
        //System.out.println("postrequest: " + postRequest);
        return postRequest0;
    }

    private static void checkNumber(JSONObject pOptions, Object userValue) throws IncompatibleValueException {
        double min = pOptions.getDouble("min");
        double max = pOptions.getDouble("max");
        double steps = pOptions.getDouble("steps");
        double k = min;
        double userValueD;
        if (userValue != null) {
            userValueD = new Double(userValue.toString());
            for (; k <= max; k = k + steps) {
                if (userValueD - k < 0.000001) {
                    break;
                }
            }
            if (k <= max) {
                parameters.put(pName, userValueD);
                ++countmatch;
            } else {
                throw new IncompatibleValueException("Not accepted value for parameter key " + pName);
            }
        } else {   // if userValue == null
            double defaultValue = pOptions.getDouble("default");
            parameters.put(pName, defaultValue);
            ++countmatch;
        }
    }

    // input type checks
    private static void checkSelect(JSONObject pOptions, Object userValue) throws IncompatibleValueException {
        String pValues = pOptions.getJSONArray("values").toString();
        if (userValue != null && pValues.contains(userValue.toString())) {
            parameters.put(pName, userValue.toString());
            ++countmatch;
        } else if (userValue == null) {
            int p = pOptions.getInt("default");
            String defaultValue = pOptions.getJSONArray("values").getString(p);
            parameters.put(pName, defaultValue);
            ++countmatch;
        } else {
            throw new IncompatibleValueException("Not accepted value for parameter key " + pName);
        }
    }

    private static void checkFile(JSONObject pOptions, Object userValue) throws IncompatibleValueException, UserValueRequiredException {
        JSONArray mimeTypes = pOptions.getJSONObject("mimeTypes").getJSONArray("allowed");
        String[] allowedTypes = mimeTypes.toString().replace("[", "").replace("]", "").replace("\"", "").split(",");
        if (userValue != null) {
            String userMimeType = URLConnection.guessContentTypeFromName(userValue.toString());
            if (Arrays.asList(allowedTypes).contains(userMimeType)) {
                JSONObject dataObject = new JSONObject();
                dataObject.put(pName, userValue.toString());
                data.put(dataObject);
                ++countmatch;
            } else {
                throw new IncompatibleValueException("Not accepted value for parameter key " + pName);
            }
        } else {  //if userValue == null
            throw new UserValueRequiredException("User value required for this parameter");
        }
    }

    private static void logJsonObject(JSONObject object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(object.toString());
        System.out.println(gson.toJson(je));
    }
}
