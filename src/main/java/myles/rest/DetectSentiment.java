package myles.rest;
import com.opencsv.exceptions.CsvException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jdk.jshell.execution.JdiExecutionControl;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.List;
import org.json.simple.JSONArray;



@Path("detectSentiment")
public class DetectSentiment {
    private final bucketController bucketController;
    private final jsonManipulation jsonReader;

    @Inject
    public DetectSentiment(bucketController buckController, jsonManipulation jsonReader){
        this.bucketController = buckController;
        this.jsonReader = jsonReader;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void runDetectSentiments(@RequestBody List<String> phrases) throws IOException, CsvException {
        Region region = Region.US_EAST_2;
        ComprehendClient sentimentClient = ComprehendClient.builder().region(region).build();
        StartSentimentDetectionJobResponse startAnalyzerResponse = startDetectSentimentsJob(sentimentClient);
        String jobID = receiveDetectSentimentJobID(sentimentClient, startAnalyzerResponse);
        sentimentClient.close();
        String bucketName = System.getenv("bucketName");
        String keyName = "daily/" + System.getenv("accountID") + "-SENTIMENT-" + jobID + "/output/output.tar.gz";
        bucketController.getBucket(bucketName, keyName);
        JSONArray sentimentJsonArr = jsonReader.txtFiletoJsonArray(Paths.get("../../../../../../src/downloads/output").toAbsolutePath().normalize().toString());
        sentimentJsonArr = jsonReader.combinePhrasesWithSentimentJson(sentimentJsonArr, phrases);
        JSONObject sentimentDataObject = jsonReader.organizeData(sentimentJsonArr);
    }
    public static StartSentimentDetectionJobResponse startDetectSentimentsJob(ComprehendClient sentimentClient) throws CsvException, IOException {
        try {
            StartSentimentDetectionJobResponse sentimentDetectionJob = sentimentClient.startSentimentDetectionJob(builder -> builder
                    .dataAccessRoleArn(System.getenv("arn"))
                    .inputDataConfig(InputDataConfig.builder()
                            .s3Uri(System.getenv("s3InputURI"))
                            .inputFormat(InputFormat.ONE_DOC_PER_LINE)
                            .build())
                    .outputDataConfig(OutputDataConfig.builder()
                            .s3Uri(System.getenv("s3OutputURI"))
                            .build())
                    .languageCode("en")
            );
            return (sentimentDetectionJob);

        } catch (ComprehendException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(0);
            return null;
        }
    }
    public static String receiveDetectSentimentJobID(ComprehendClient sentimentClient, StartSentimentDetectionJobResponse startResponse) throws CsvException, IOException {
        String jobID = "";
        try {
            for(int attempt = 0; attempt <= 10; attempt++){
                System.out.println("checking sentiment, attempt: " + attempt);
                SentimentDetectionJobProperties describeResponse =
                        sentimentClient.describeSentimentDetectionJob(builder -> builder.jobId(startResponse.jobId())).sentimentDetectionJobProperties();
                if(describeResponse.jobStatus().toString().equals("COMPLETED")) {
                    System.out.println("COMPLETED: " + describeResponse.message());
                    System.out.println("jobID: " + describeResponse.jobId());
                    jobID = describeResponse.jobId();
                    return describeResponse.jobId();
                } else if (describeResponse.jobStatus().toString().equals("FAILED")) {
                    System.out.println("FAILED: " + describeResponse.message());
                }
                else{
                    System.out.println(describeResponse.jobStatus() + ": " + describeResponse.message());
                }
                try{
                    Thread.sleep(120 * 1000);
                } catch(InterruptedException e){
                    System.err.println("Error while waiting for job completion: " + e.getMessage());
                    return "err: 2"; // Indicate error during waiting
                }
            }
            return (jobID);

        } catch (ComprehendException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return "err: 1";
        }
    }

}
