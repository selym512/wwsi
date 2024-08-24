package myles.rest;

import com.opencsv.exceptions.CsvException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.print.attribute.standard.Media;
import java.io.IOException;

@Path("dailySentiment")
public class DailySentimentController {

    private final MongoManager mongoManager;
    private final DetectSentiment detectSentiment;

    @Inject
    public DailySentimentController(MongoManager mongoManager, DetectSentiment detectSentiment){
        this.mongoManager = mongoManager;
        this.detectSentiment = detectSentiment;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Object deleteDailySentiment(@RequestBody String ID){
        return (mongoManager.deleteDailySentiment(ID));

    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getSentimentData(){
        System.out.println("get request received");
        return mongoManager.getSentimentData().toJson();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void detectSentiment(@RequestBody String putString) throws IOException, CsvException {
        detectSentiment.runDetectSentiments(putString);
    }
}
