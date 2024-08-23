package myles.rest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SentimentData {
    public SentimentData(){

    }

    public JSONArray combinePhrasesWithSentiment(JSONArray lineSentiment, List<String> phrases){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i <= lineSentiment.size() - 1; i++){
            JSONObject jsonO = (JSONObject) lineSentiment.get(i);
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
        for(int i = 0; i <= sentimentJsonArr.size() - 1; i++) {
            JSONObject jsonO = (JSONObject) sentimentJsonArr.get(i);
            if(jsonO.get("Sentiment").toString().equals("NEGATIVE")){
                negativeArray.add(jsonO);
            } else if (jsonO.get("Sentiment").toString().equals("POSITIVE")){
                positiveArray.add(jsonO);
            }
        }
        organizedData.put("positive", positiveArray);
        organizedData.put("negative", negativeArray);
        System.out.println(organizedData);
        return organizedData;
    }

    public ArrayList<String> txtFileToArray(String file) {
        try {
            ArrayList<String> lineSentiment = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.lines().forEach(lineSentiment::add);
            System.out.println(lineSentiment.toString());
            reader.close();
            return lineSentiment;
        }
        catch(NullPointerException | IOException e){
            e.printStackTrace();
            System.err.println(e + e.getMessage() + e.getCause() + e.getLocalizedMessage());
            return new ArrayList<String>();
        }
    }

}
