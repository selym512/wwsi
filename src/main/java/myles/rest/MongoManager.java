package myles.rest;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class MongoManager {

    MongoClient mongoClient;

    @Inject
    public MongoManager(){
        String uri = "mongodb://localhost:27017/newsData";
        try (MongoClient mongoC = MongoClients.create(uri)) {
            this.mongoClient = mongoC;
        }
    }
    public void insert(){
        MongoDatabase database = mongoClient.getDatabase("newsData");
        MongoCollection<Document> collection = database.getCollection("positivePhrases");
        // Creates two sample documents containing a "title" field
        List<Document> phraseList = Arrays.asList(
                new Document().append("phrase", "David Attenborough’s colorful impact on Wimbledon and tennis: yellow balls"),
                new Document().append("phrase", "Excitement levels rise among fans at European Championship"));
        try {
            // Inserts sample documents describing movies into the collection
            InsertManyResult result = collection.insertMany(phraseList);
            // Prints the IDs of the inserted documents
            System.out.println("Inserted document ids: " + result.getInsertedIds());

            // Prints a message if any exceptions occur during the operation
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }
    public void deleteAll(){
        MongoDatabase database = mongoClient.getDatabase("newsData");
        MongoCollection<Document> collection = database.getCollection("positivePhrases");
        Bson query = new Document();
        try {
            // Deletes all documents that have an "imdb.rating" value less than 1.9
            DeleteResult result = collection.deleteMany(query);

            // Prints the number of deleted documents
            System.out.println("Deleted document count: " + result.getDeletedCount());

            // Prints a message if any exceptions occur during the operation
        } catch (MongoException me) {
            System.err.println("Unable to delete due to an error: " + me);
        }


    }

}
