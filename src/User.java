import java.util.ArrayList;

public class User {
    private String id;
    private ArrayList<String> amici;
    private int score;

    public User(String name) {
        this.id = name;
        this.score = 0;
        this.amici = new ArrayList<>();
    }

    public String getId() {
        return this.id;
    }

    public boolean addFriend(String username) {
        return this.amici.add(username);
    }

    public ArrayList<String> getFriends() {
        return this.amici;
    }
}
