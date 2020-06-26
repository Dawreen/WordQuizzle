import com.google.gson.Gson;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Classe che gestisce gli utenti, password e session degli utenti che sono online.
 */
public class UserCollection {
    private HashMap<String, User> allUsers;
    private File userFile;

    private HashMap<String, String> passwords;
    private File passFile;

    private HashMap<String, Session> online;

    /**
     * Inizializza l'insieme di tutti gli user e delle loro password.
     * Nel caso non ci siano file con i dati degli utenti/password essi vengono creati.
     * Nel caso in cui i file esistano le strutture dati si aggiornano dai file.
     * @param userFile string con la path degli user
     * @param passFile string con la path delle password
     */
    public UserCollection(String userFile, String passFile) {
        this.online = new HashMap<>();

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
     * Utilizzando i file di configurazione inizializza le strutture dati del programma.
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

    /**
     * Aggiorna il file con le registrazioni(username, password).
     */
    private synchronized void registrationUpdate (){
        Set<String> setUserName = this.passwords.keySet();
        String[] auxString = new String[setUserName.size()*2];
        int i = 0;
        for (String each : setUserName) {
            auxString[i] = each;
            auxString[++i] = this.passwords.get(each);
            i++;
        }
        try (Writer writer = new FileWriter(passFile, false)) {
            Gson gson = new Gson();
            String passString = gson.toJson(auxString);

            writer.write(passString);
        }catch (IOException e) {
            System.err.println("SHUTDOWN - exception file update");
            e.printStackTrace();
        }
    }

    /**
     * Crea e aggiunge un nuovo utente.
     * @param username stringa dell'id del nuovo utente.
     * @param password password per l'accesso.
     * @return true in caso di aggiunta avvenuta con successo.
     *         false nel caso non sia stato possibile aggiungere l'utente.
     */
    public synchronized boolean addUser(String username, String password) {
        if (this.allUsers.containsKey(username)) return false;
        else {
            this.allUsers.put(username, new User(username));
            this.passwords.put(username, password);
            updateFile();
            registrationUpdate();
            return true;
        }
    }

    /**
     * Controllo se l'utente è registrato.
     * @param username id dell'utente del quale si vuole sapere l'avvenuta registrazione
     * @return true se l'utente è registrato
     *         false altrimenti
     */
    public synchronized boolean checkRegistration(String username) {
        return this.allUsers.containsKey(username);
    }

    /**
     * Controlla se un utente è online.
     * @param username id dell'utente
     * @return true se l'utente è online
     *         false altrimenti
     */
    public synchronized boolean checkOnline(String username) {
        return this.online.containsKey(username);
    }

    /**
     * Controlla che l'utente abbia inserito la password corretta.
     * @param username id dell'utente.
     * @param password stringa con i caratteri da controllare.
     * @return true se la password combacia con quella nel "database".
     *         false altrimenti.
     */
    public synchronized User checkPass(String username, String password) {
        if (this.passwords.get(username).equals(password)) {
            return allUsers.get(username);
        } else {
            return null;
        }
    }

    /**
     * Aggiunge un utente a quelli online.
     * @param username id dell'utente da aggiungere online.
     * @param session sessione al quale l'utente è connesso.
     */
    public synchronized void addOnline(String username, Session session) {
        this.online.put(username, session);
    }

    /**
     * Rimuove un utente dal insieme degli utenti online.
     * @param username id dell'utente da rimuovere.
     */
    public synchronized void removeOnline(String username) {
        this.online.remove(username);
    }

    /**
     * Restituisce l'istanza di un dato utente online.
     * @param username id dell'utente interessato.
     * @return istanza della sessione.
     */
    public synchronized Session getSession(String username) {
        return this.online.get(username);
    }

    /**
     * Aggiunge due utenti alla rispettiva cerchia delle amicizie
     * @param user1 id del utente che aggiunge
     * @param user2 id dell'utente che viene aggiunto
     */
    public synchronized void aggiungiAmicizia(String user1, String user2) {
        this.allUsers.get(user1).addFriend(user2);
        this.allUsers.get(user2).addFriend(user1);
        updateFile();
    }

    public int userScore(String user) {
        if (this.allUsers.containsKey(user)) {
            return this.allUsers.get(user).getScore();
        } else {
            return -1;
        }
    }
}
