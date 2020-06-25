import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * classe che si occupa del dizionario delle parole.
 * Importa le parole da un file di testo e genera n paroli casuali a richiesta.
 */
public class Dictionary {
    private ArrayList<String> allWords;

    /**
     * Prende le parole da un file e le "mette" nel programma.
     * (è necessario che ci sia una parola per ogni rigo)
     * @param dictionaryPath path dal quale prendere le parole
     */
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

    /**
     * Genera il numero di parole richiesto.
     * @param n numero di parole che si vogliono generare
     * @return restituisce un array[n] contenente le String delle parole richieste
     */
    public String[] getWords(int n) {
        String[] words = new String[n];
        for (int i = 0; i < n; i++) {
            int index = (int) ((Math.random()) * (allWords.size()-1));
            words[i] = allWords.get(index);
        }
        return words;
    }
}
