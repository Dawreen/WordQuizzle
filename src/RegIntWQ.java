import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegIntWQ extends Remote {
    boolean registration(String name, char[] password) throws RemoteException;
}
