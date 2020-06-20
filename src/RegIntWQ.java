import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegIntWQ extends Remote {
    boolean registration(String name, String password) throws RemoteException;
}
