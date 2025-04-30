package myles.rest;

import java.util.List;
import org.json.simple.JSONArray;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class jsonManipulationTest {

    private JSONArray sentimentJsonArr;
    private List<String> phrases;

    @Before
    public void setup(){
        JSONArray sentimentJsonArr = mock(JSONArray.class);
        List<String> phrases = List.of("POSITIVE", "NEGATIVE");
    }

    @Test
    void combinePhrasesWithSentimentJson() {
    }

    @Test
    void organizeData() {
    }

    @Test
    void txtFiletoJsonArray() {
    }
}