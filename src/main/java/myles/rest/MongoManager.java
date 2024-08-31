package myles.rest;
import com.mongodb.MongoException;
import com.mongodb.client.*;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
public class MongoManager {

    String uri = "mongodb://localhost:27017/newsData";
    private final MongoClient mongoClient = MongoClients.create(uri);
    private final MongoDatabase database = this.mongoClient.getDatabase("newsData");


    public BsonDocument getSentimentData(){
        MongoCollection<Document> dailySentimentCollection = database.getCollection("dailySentiment");
        MongoCollection<Document> positivePhrasesCollection = database.getCollection("positivePhrases");
        MongoCollection<Document> negativePhrasesCollection = database.getCollection("negativePhrases");

        MongoCursor<Document> cursor = dailySentimentCollection.find().iterator();
        BsonArray dailySentimentJSON = new BsonArray();
        try {
            while (cursor.hasNext()) {
                dailySentimentJSON.add(cursor.next().toBsonDocument());
            }
        } finally {
            cursor.close();
        }
        cursor = positivePhrasesCollection.find().iterator();
        BsonArray positivePhraseJSON = new BsonArray();
        try {
            while (cursor.hasNext()) {
                positivePhraseJSON.add(cursor.next().toBsonDocument());
            }
        } finally {
            cursor.close();
        }
        cursor = negativePhrasesCollection.find().iterator();
        BsonArray negativePhraseJSON = new BsonArray();
        try {
            while (cursor.hasNext()) {
                negativePhraseJSON.add(cursor.next().toBsonDocument());
            }
        } finally {
            cursor.close();
        }

        BsonDocument sentimentData = new BsonDocument();
        sentimentData.put("positive", positivePhraseJSON);
        sentimentData.put("negative", negativePhraseJSON);
        sentimentData.put("dailySentiment", dailySentimentJSON);

        return sentimentData;
    }

    public void insertDailySentimentIndex(JSONObject sentimentJson){
        MongoCollection<Document> dailySentimentCollection = database.getCollection("dailySentiment");
        JSONArray positiveJSONArray = (JSONArray)sentimentJson.get("positive");
        JSONArray negativeJSONArray = (JSONArray)sentimentJson.get("negative");
        JSONArray neutralJSONArray = (JSONArray)sentimentJson.get("neutral");

        float positiveScore = (float) positiveJSONArray.size() / (positiveJSONArray.size() + negativeJSONArray.size() + neutralJSONArray.size());
        float negativeScore = (float) negativeJSONArray.size() / (positiveJSONArray.size() + negativeJSONArray.size() + neutralJSONArray.size());
        System.out.println(positiveScore);
        System.out.println(negativeScore);
        Document wwsiDoc = new Document("_id", new ObjectId())
                .append("positiveScore", positiveScore)
                .append("negativeScore", negativeScore)
                .append("date", new Date());
        dailySentimentCollection.insertOne(wwsiDoc);
    }
    public void insertDailySentimentIndexZero(JSONObject sentimentJson){
        MongoCollection<Document> dailySentimentCollection = database.getCollection("dailySentiment");
        JSONArray negativeJSONArray = (JSONArray)sentimentJson.get("negative");
        JSONArray neutralJSONArray = (JSONArray)sentimentJson.get("neutral");
        float negativeScore = (float) negativeJSONArray.size() / (negativeJSONArray.size() + neutralJSONArray.size());
        System.out.println(negativeScore);
        Document wwsiDoc = new Document("_id", new ObjectId())
                .append("positiveScore", 0)
                .append("negativeScore", negativeScore)
                .append("date", new Date());
        dailySentimentCollection.insertOne(wwsiDoc);
    }

    public void insertPhraseSentimentJson(JSONObject sentimentJson) throws IllegalArgumentException{
        MongoCollection<Document> positivePhrasesCollection = database.getCollection("positivePhrases");
        MongoCollection<Document> negativePhrasesCollection = database.getCollection("negativePhrases");

        List<Document> posPhraseList = new ArrayList<Document>();
        JSONArray positiveJSONArray = (JSONArray)sentimentJson.get("positive");
        Iterator it = positiveJSONArray.iterator();
        while (it.hasNext()) {
            posPhraseList.add(new Document().append("phrase", it.next()));
        }

        List<Document> negPhraseList = new ArrayList<Document>();
        JSONArray negativeJSONArray = (JSONArray)sentimentJson.get("negative");
        it = negativeJSONArray.iterator();
        while (it.hasNext()) {
            negPhraseList.add(new Document().append("phrase", it.next()));
        }

        try {
            // Inserts phrase documents into the collection
            InsertManyResult nResult = negativePhrasesCollection.insertMany(negPhraseList);
            InsertManyResult pResult = positivePhrasesCollection.insertMany(posPhraseList);

            // Prints the IDs of the inserted documents
            System.out.println("Inserted document ids: " + nResult.getInsertedIds());
            System.out.println("Inserted document ids: " + pResult.getInsertedIds());

        } catch (MongoException me) {
            // Prints a message if any exceptions occur during the operation
            System.err.println("Unable to insert due to an error: " + me);
        }
    }
    public void insertPhraseSentimentJsonZeroPositive(JSONObject sentimentJson) throws IllegalArgumentException{
        MongoCollection<Document> negativePhrasesCollection = database.getCollection("negativePhrases");

        List<Document> negPhraseList = new ArrayList<Document>();
        JSONArray negativeJSONArray = (JSONArray)sentimentJson.get("negative");
        Iterator it = negativeJSONArray.iterator();
        while (it.hasNext()) {
            negPhraseList.add(new Document().append("phrase", it.next()));
        }

        try {
            // Inserts phrase documents into the collection
            InsertManyResult nResult = negativePhrasesCollection.insertMany(negPhraseList);

            // Prints the IDs of the inserted documents
            System.out.println("Inserted document ids: " + nResult.getInsertedIds());
            System.out.println("Inserted document ids: " + 0);

        } catch (MongoException me) {
            // Prints a message if any exceptions occur during the operation
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void deleteAllPhrases(){
        MongoCollection<Document> posCollection = database.getCollection("positivePhrases");
        MongoCollection<Document> negCollection = database.getCollection("negativePhrases");
        Bson query = new Document();
        try {
            DeleteResult result = posCollection.deleteMany(query);
            // Prints the number of deleted documents
            System.out.println("Deleted positive document count: " + result.getDeletedCount());
            result = negCollection.deleteMany(query);
            // Prints the number of deleted documents
            System.out.println("Deleted negative document count: " + result.getDeletedCount());

        } catch (MongoException me) {
            // Prints a message if any exceptions occur during the operation
            System.err.println("Unable to delete due to an error: " + me);
        }
    }
}
