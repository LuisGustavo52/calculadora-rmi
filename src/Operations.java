import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Operations extends Remote {
    Number sum(Number x, Number y) throws RemoteException;
    Number sub(Number x, Number y) throws RemoteException;
    Number mul(Number x, Number y) throws RemoteException;
    Number div(Number x, Number y) throws RemoteException;
    Number pow(Number exponent, Number base) throws RemoteException;
    Number root(Number exponent, Number base) throws RemoteException;
    Number percent(Number x, Number percent) throws RemoteException;
    Number mod(Number x, Number y) throws RemoteException;
    Number factorial(Number x) throws RemoteException;
    Number decimalToBinary(Number x) throws RemoteException;
    Number binaryToDecimal(Number x) throws RemoteException;
    Number decimalToHex(Number x) throws RemoteException;
    Number hexToDecimal(Number x) throws RemoteException;
    Number binaryToHex(Number x) throws RemoteException;
    Number hexToBinary(Number x) throws RemoteException;
    List<String> lastOperations(int howMany) throws RemoteException;
    List<String> lastOperations() throws RemoteException;
}