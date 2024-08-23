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

    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoManager() {
        String uri = "mongodb://localhost:27017/newsData";
        this.mongoClient = MongoClients.create(uri);
        this.database = this.mongoClient.getDatabase("newsData");

    }

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

    public Object deleteDailySentiment(String ID){
        MongoCollection<Document> dailySentimentCollection = database.getCollection("dailySentiment");
        try {
            System.out.println(ID);
            Bson query = eq("_id", new ObjectId(ID));
            DeleteResult result = dailySentimentCollection.deleteOne(query);
            System.out.println("Deleted document count: " + result.getDeletedCount());
            return("Deleted document count: " + result.getDeletedCount());
            // Prints a message if any exceptions occur during the operation
        } catch (MongoException me) {
            System.err.println("Unable to delete due to an error: " + me);
            return (me);
        }
    }

    public void insertDailySentimentIndex(JSONObject sentimentJson){
        MongoCollection<Document> dailySentimentCollection = database.getCollection("dailySentiment");
        JSONArray positiveJSONArray = (JSONArray)sentimentJson.get("positive");
        JSONArray negativeJSONArray = (JSONArray)sentimentJson.get("negative");
        float WWSI = (float) positiveJSONArray.size() / (positiveJSONArray.size() + negativeJSONArray.size());
        System.out.println(WWSI);
        Document wwsiDoc = new Document("_id", new ObjectId())
                .append("value", WWSI)
                .append("date", new Date());
        dailySentimentCollection.insertOne(wwsiDoc);
    }

    public void insertPhraseSentimentJson(JSONObject sentimentJson){
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
            InsertManyResult pResult = positivePhrasesCollection.insertMany(posPhraseList);
            InsertManyResult nResult = negativePhrasesCollection.insertMany(negPhraseList);

            // Prints the IDs of the inserted documents
            System.out.println("Inserted document ids: " + pResult.getInsertedIds());
            System.out.println("Inserted document ids: " + nResult.getInsertedIds());


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
