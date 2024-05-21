package myles.rest;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class csvManipulation {
    public String getDailySentiment() throws IOException, CsvException {
        CSVReader reader = new CSVReaderBuilder(new FileReader("dailySentiment.csv")).build();
        List<String[]> myEntries = reader.readAll();
        return myEntries.toString();
    }
    public void writeDailySentiment() throws IOException {
        CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter("dailySentiment.csv"))
                .withSeparator('\t')
                .build();
        // feed in your array (or convert your data to an array)
        String[] entries = "first#second#third".split("#");
        writer.writeNext(entries);
        writer.close();
    }

    public String getPhrases() throws IOException, CsvException {
        try (InputStream resourceStream =  getClass().getResourceAsStream("/phrases.csv");
             Reader reader = new InputStreamReader(resourceStream)) {
            CSVReader csvReader = new CSVReaderBuilder(reader).build();
            String[] positivePhrases = csvReader.readNext();
            positivePhrases = Arrays.copyOfRange(positivePhrases, 1, positivePhrases.length);
            String[] negativePhrases = csvReader.readNext();
            negativePhrases = Arrays.copyOfRange(negativePhrases, 1, negativePhrases.length);
            return positivePhrases[0] + negativePhrases[0];
        }
    }

    public void writePhrases() throws IOException{
        CSVWriter writer = (CSVWriter) new CSVWriterBuilder(new FileWriter("dailySentiment.csv"))
                .withSeparator('\t')
                .build();
        // feed in your array (or convert your data to an array)
        String[] entries = "first#second#third".split("#");
        writer.writeNext(entries);
        writer.close();
    }

}
