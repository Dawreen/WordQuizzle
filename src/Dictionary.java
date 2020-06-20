import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class Dictionary {
    private ArrayList<String> allWords;

    public Dictionary(String dictionaryPath) {
        try (Reader reader = new FileReader(dictionaryPath)) {
            this.allWords = new ArrayList<>();
            StringBuilder str = new StringBuilder();
            int intChar;

            do {
                intChar = reader.read();
                if ((char)intChar != '\r') {
                    if ((char)intChar != '\n')
                        str.append((char) intChar);
                } else {
                    this.allWords.add(str.toString());
                    str.delete(0, str.length());
                }
            } while (intChar != -1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getWords(int n) {
        String[] words = new String[n];
        for (int i = 0; i < n; i++) {
            int index = (int) ((Math.random()) * (allWords.size()));
            words[i] = allWords.get(index);
        }
        return words;
    }
}
