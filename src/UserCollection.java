import com.google.gson.Gson;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

public class UserCollection {
    private HashMap<String, User> allUsers;
    private File userFile;

    private HashMap<String, String> passwords;
    private File passFile;

    /**
     * Inizializza l'insieme di tutti gli user e delle loro password.
     * Nel caso non ci siano file con i dati degli utenti/password essi vengono creati.
     * Nel caso in cui i file esistano le strutture dati si aggiornano dai file.
     * @param userFile string con la path degli user
     * @param passFile string con la path delle password
     */
    public UserCollection(String userFile, String passFile) {
        this.userFile = new File(userFile);
        this.passFile = new File(passFile);
        try {
            if (this.userFile.createNewFile() || this.passFile.createNewFile()) {
                this.allUsers = new HashMap<>();
                this.passwords = new HashMap<>();
            } else {
                this.updateData();

                String passString = "";

                //inizialising the hashmap containing the passwords
                try (Reader reader = new FileReader(this.passFile)) {
                    StringBuilder passBuild = new StringBuilder();

                    int charAsInt = reader.read();
                    while (charAsInt != -1) {
                        passBuild.append((char) charAsInt);
                        charAsInt = reader.read();
                    }

                    passString = passBuild.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String[] passArray = new Gson().fromJson(passString, String[].class);

                if (passArray != null) { // controllo che il file passato non sia vuoto
                    this.passwords = new HashMap<>(passArray.length / 2);
                    for (int i = 0; i < passArray.length; i += 2) {
                        this.passwords.put(passArray[i], passArray[i + 1]);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Utilizzando i file diconfigurazione inizializza le strutture dati del programma.
     */
    private synchronized void updateData() {
        String userString = "";

        try (Reader reader = new FileReader(userFile)) {
            StringBuilder userBuild = new StringBuilder();

            int charAsInt = reader.read();
            while (charAsInt != -1) {
                userBuild.append((char) charAsInt);
                charAsInt = reader.read();
            }

            userString = userBuild.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        User[] usersArray = new Gson().fromJson(userString, User[].class);

        this.allUsers = new HashMap<>();
        if (usersArray != null) { // controllo che il contenuto del file non sia vuoto
            for (User each : usersArray) {
                this.allUsers.put(each.getId(), each);
            }
        }
    }

    /**
     * Utilizzando le strutture dati del programma aggiorna i file json.
     */
    public synchronized void updateFile() {
        try (Writer writer = new FileWriter(this.userFile, false)) {
            Collection<User> temp = this.allUsers.values();
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            User[] userArray = temp.toArray(new User[temp.size()]);
            Gson gson = new Gson();
            String userString = gson.toJson(userArray);

            writer.write(userString);
        }catch (IOException e) {
            System.err.println("UPDATEFILE - exception");
            e.printStackTrace();
        }
    }
}
