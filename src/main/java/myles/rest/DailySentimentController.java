package myles.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

@Path("dailySentiment")
public class DailySentimentController {

    private final MongoManager mongoManager;

    @Inject
    public DailySentimentController(MongoManager mongoManager){
        this.mongoManager = mongoManager;
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

}
