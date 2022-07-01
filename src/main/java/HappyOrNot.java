import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;

public class HappyOrNot {

    public Entry[] entries;

    public static class Entry {
        public String ts;
        public LocalDate date;
        public int r;
        public double relevance;
        public boolean spam;
        public String text;

        public int getRating() {
            switch (r) {
                case 0:
                    return 1;
                case 1:
                    return 2;
                case 2:
                    return 4;
                case 3:
                    return 5;
                default:
                    return 0;
            }
        }

        public void setDate() {
            this.date = LocalDate.parse(ts.split("T")[0]);
        }

        public boolean isFeedback() {
            return text != null;
        }
    }

    public void readDataFromFile() {
        ObjectMapper objectMapper = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
                .build();
        try {
            String file = "src/main/resources/feedback.json";
            String json = new String(Files.readAllBytes(Paths.get(file)));
            entries = objectMapper.readValue(json, Entry[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}
