import java.util.ArrayList;

/**
 * Astrazione dell'utente.
 */
public class User {
    private String id; // username
    private ArrayList<String> amici; // cerchia degli amici
    private int score; // punteggio nella classifica generale

    public static final int Z = 1;

    /**
     * Metodo costruttore.
     * @param name username dell'utente
     */
    public User(String name) {
        this.id = name;
        this.score = 0;
        this.amici = new ArrayList<>();
    }

    /**
     * Getter dell'id
     * @return stringa con username
     */
    public String getId() {
        return this.id;
    }

    /**
     * Aggiunge un amico all cerchia delgi amici.
     * @param username stringa dell'utente che si vuole aggiungere.
     * @return true se l'aggiunta è avvenuta con successo.
     *         false se l'utente è già presente nella cerchia.
     */
    public boolean addFriend(String username) {
        if (!this.amici.contains(username)) {
            return this.amici.add(username);
        } else {
            return false;
        }
    }

    /**
     * Getter della cerchia degli amici
     * @return arraylist contente gli id di tutti gli amici
     */
    public ArrayList<String> getFriends() {
        return this.amici;
    }

    /**
     * Aggiunge allo score dello user.
     */
    public void addPoints() {
        this.score = this.score + Z;
    }

    /**
     * Gettere dello score.
     * @return int con il punteggio totalizzato fin'ora.
     */
    public int getScore() {
        return this.score;
    }
}
