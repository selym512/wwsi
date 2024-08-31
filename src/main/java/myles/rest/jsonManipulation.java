package myles.rest;
import java.io.*;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
@ApplicationScoped
public class jsonManipulation {

    public JSONArray combinePhrasesWithSentimentJson(JSONArray sentimentJsonArr, List<String> phrases){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i <= sentimentJsonArr.size() - 1; i++){
            JSONObject jsonO = (JSONObject) sentimentJsonArr.get(i);
            int lineNumber = Math.toIntExact((Long) jsonO.get("Line"));
            jsonO.put("Phrase", phrases.get(lineNumber));
            jsonArray.add(jsonO);
        }
        return jsonArray;
    }
    public JSONObject organizeData(JSONArray sentimentJsonArr){
        JSONObject organizedData = new JSONObject();
        JSONArray negativeArray = new JSONArray();
        JSONArray positiveArray = new JSONArray();
        JSONArray neutralArray = new JSONArray();
        for(int i = 0; i <= sentimentJsonArr.size() - 1; i++) {
            JSONObject jsonO = (JSONObject) sentimentJsonArr.get(i);
            if(jsonO.get("Sentiment").toString().equals("NEGATIVE")){
                negativeArray.add(jsonO);
            } else if (jsonO.get("Sentiment").toString().equals("POSITIVE")){
                positiveArray.add(jsonO);
            }
            else {
                neutralArray.add(jsonO);
            }
        }
        organizedData.put("positive", positiveArray);
        organizedData.put("negative", negativeArray);
        organizedData.put("neutral", neutralArray);
        return organizedData;
    }

    public JSONArray txtFiletoJsonArray(String file) {
        try {
            JSONArray jsonArray = new JSONArray();
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            Object jsonObj = parser.parse(line);
            JSONObject jsonObject = (JSONObject) jsonObj;

            do {
                jsonObj = parser.parse(line);
                jsonObject = (JSONObject) jsonObj;
                jsonArray.add(jsonObject);
                line = reader.readLine();
            }
            while (line != null);

            reader.close();
            return jsonArray;
        }
        catch(NullPointerException | ParseException | IOException e){
            e.printStackTrace();
            System.err.println(e + e.getMessage() + e.getCause() + e.getLocalizedMessage());
            return new JSONArray();
        }
    }

}
