package src;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.util.List;

public class QuestionLoader {
    public static List<Question> loadQuestions(String filePath) {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(filePath);
            List<Question> questions = gson.fromJson(reader, new TypeToken<List<Question>>(){}.getType());
            reader.close();
            return questions;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
