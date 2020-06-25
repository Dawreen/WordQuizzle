import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegIntWQ extends Remote {
    /**
     * Metodo per la registrazione di un nuovo utente al servizio.
     * @param name id con il quale si vuole registrare
     * @param password codice per effetuare l'accesso al login
     * @return 0 : la registrazione è andata a buon fine
     *         1 : name o password sono vuoti
     *         2 : username già in uso
     * @throws RemoteException
     */
    int registration(String name, String password) throws RemoteException;
}
