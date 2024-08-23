package myles.rest;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class bucketController {

    S3Client s3Client;

    @Inject
    public bucketController() {
        this.s3Client = S3Client.builder().region(Region.US_EAST_2).build();
    }

    public void getBucket(String bucketName, String keyName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();
            Path p = Paths.get("../../../../../../output.tar.gz").toAbsolutePath().normalize();
            Path pUnzip = Paths.get("../../../../../../output").toAbsolutePath().normalize();
            Files.deleteIfExists(p);
            Files.deleteIfExists(pUnzip);
            GetObjectResponse getObjectResponse = s3Client.getObject(getObjectRequest, p);
            System.out.println("Downloaded object");
            InputStream fi = Files.newInputStream(Paths.get("../../../../../../output.tar.gz").toAbsolutePath().normalize());
            BufferedInputStream bi = new BufferedInputStream(fi);
            GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
            TarArchiveInputStream ti = new TarArchiveInputStream(gzi);
            ArchiveEntry entry;
            while ((entry = ti.getNextEntry()) != null) {
                // create a new path, remember check zip slip attack
                Path outPath = Paths.get("../../../../../../output").toAbsolutePath().normalize();
                //checking
                // copy TarArchiveInputStream to newPath
                Files.copy(ti, outPath);
            }
        }
        catch (IOException | NullPointerException | SdkClientException e) {
            System.err.println(e.toString());
        }
        catch (Exception ee){
            System.err.println(ee.toString() + ee.getMessage() + ee.getCause() + ee.getLocalizedMessage());
        }
    }
}
