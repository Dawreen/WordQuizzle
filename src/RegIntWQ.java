import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegIntWQ extends Remote {
    int registration(String name, String password) throws RemoteException;
    // TODO: 21/06/2020 codice ritorno: avvenuta registrazione, nick già presente, password vuota
}
