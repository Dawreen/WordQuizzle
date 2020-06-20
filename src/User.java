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

    public String[] getFriends() {
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return this.amici.toArray(new String[this.amici.size()]);
    }
}
